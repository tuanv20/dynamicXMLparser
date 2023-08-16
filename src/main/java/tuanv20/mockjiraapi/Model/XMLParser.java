package tuanv20.mockjiraapi.Model;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;
import com.atlassian.jira.rest.client.api.domain.Issue;
import tuanv20.mockjiraapi.JIRALogger;
import tuanv20.mockjiraapi.Controller.JiraController;

@Component
public class XMLParser {
    public static final String[] dataTags = {"TIME", "DELTA_AZ", "DELTA_EL", "TLM_FR", "CMD"};

    @Value("${paths.img_path}")
    private String IMG_DIR_PATH;

    @Autowired
    Linechart linechart;

    @Autowired
    JIRALogger log;

    public XMLParser() {
    }

    /**
    * Parses the XML file that was modified and populates the JIRAIssue data object.
    * This JIRAIssue is then used to create or update the an issue on the JIRA instance 
    * with visualizations and fields parsed from the XML file
    *
    * @param  issue                 JIRAIssue data object to be populated
    * @param  filePath              Path of the file that was modified
    * @param  fileName              Name of the file that was modified 
    *
    **/
    public void parseXML(JIRAIssue issue, Path filePath, String fileName, JiraController JIRAController){
        try{
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new File(filePath.toString()));
            Element root = doc.getDocumentElement();
            String projKey = root.getAttribute("project_id");
            String contactID = root.getAttribute("contact_id");
            issue.setProjKey(projKey);
            issue.setID(contactID);

            //http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            root.normalize();
                NodeList dataList = root.getChildNodes();
                for (int i = 0; i < dataList.getLength(); i++) {
                    Node dataNode = dataList.item(i);
                    //Filters out empty text nodes and processes relevant nodes
                    if(dataNode.getNodeType() == Node.ELEMENT_NODE){
                        processDataNode(dataNode, doc, issue);
                    }
                }
            //Visualization logic here 
            linechart.createLineChart(issue);
            File tlm_fr_attachment = linechart.exportAsPng(fileName.substring(0, fileName.lastIndexOf('.')) + ".png");
            issue.addAttachment(tlm_fr_attachment);
            //Searches for issue JIRA key by Contact ID of issue; returns null if it doesn't exist 
            Iterable<Issue> matchedIssues = JIRAController.getIssueKeyByContactID(issue.getID());
            if(matchedIssues.iterator().hasNext() == false){
                String issueKey = issue.createJIRAIssue(issue, fileName);
                log.info("Successfully Created JIRA issue: " + issueKey);
            }
            else{
                int index = 1;
                String JIRAKey = null; 
                String mainIssueKey = null; 
                for(Issue matchIssue: matchedIssues){
                    if(index == 1){
                        JIRAKey = matchIssue.getKey();
                    }
                    else{
                        mainIssueKey = matchIssue.getKey();
                    }
                    index++;
                }
                issue.setJIRAKey(JIRAKey);
                issue.setMainIssueKey(mainIssueKey);
                issue.updateJIRAIssue(JIRAKey, mainIssueKey);
                log.info("Successfully Updated JIRA issue: " + JIRAKey);
            }
        }
        catch(ParserConfigurationException | SAXException | IOException e){
            log.error("Error parsing the XML file");
        }
    }

    /**
    * Helper method to parse the child nodes of the XML file. Populates 
    * the First Class Data, Parameters, and Datapoints of the issue
    *
    * @param  node                  Child Node being processed + parsed 
    * @param  doc                   Overall XML document 
    * @param  issue                 JIRAIssue being populated 
    *
    **/
    public static void processDataNode(Node node, Document doc, JIRAIssue issue) throws IOException{
        switch(node.getNodeName()){
            case "PARAMS":
                NodeList paramList = doc.getElementsByTagName("PARAM");
                for(int i = 0; i < paramList.getLength(); i++){
                    Element paramElement = (Element) paramList.item(i);
                    String name = paramElement.getElementsByTagName("NAME").item(0).getTextContent();
                    String value = paramElement.getElementsByTagName("VALUE").item(0).getTextContent();
                    issue.getParams().add(name, value);
                    }
                break;

            case "DATA":
                NodeList dataList = doc.getElementsByTagName("DATAPOINT");
                for(int i = 0; i < dataList.getLength(); i++){
                    ArrayList<String> datapointVars = new ArrayList<String>();
                    Element dataElement = (Element) dataList.item(i);
                    for(String dataTag : dataTags){
                        String value = dataElement.getElementsByTagName(dataTag).item(0).getTextContent();
                        //Convert value to epoch if processing Time datatag
                        value = dataTag.equals("TIME") ? Long.toString(dateToEpoch(value)) : value;
                        datapointVars.add(value);
                    }
                    issue.addDataPoint(datapointVars);
                }
                break;
            
            case "EVENTS":
                NodeList eventList = doc.getElementsByTagName("EVENT");
                for(int i = 0; i < eventList.getLength(); i++){
                    Element eventElem = (Element) eventList.item(i);
                    String eventName = eventElem.getElementsByTagName("NAME").item(0).getTextContent();
                    Long eventTime = dateToEpoch(eventElem.getElementsByTagName("TIME").item(0).getTextContent());
                    issue.getEvents().add(new Event(eventName, eventTime));
                }

            //First-class data element 
            default:    
                String value = node.getNodeName() == "AOS" | node.getNodeName() == "LOS" ? Long.toString(dateToEpoch(node.getTextContent())) : node.getTextContent();
                issue.getFirstClass().add(node.getNodeName(), value);
                break;
            }
        }
        
    /**
    * Converts date String to Epoch 
    * @param  issue                 Date String being converted
    **/

    private static long dateToEpoch(String date){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try{
            Date contactDate = df.parse(date);
            long epoch = contactDate.getTime();
            return epoch;
        }
        catch(ParseException e){e.printStackTrace(); return 0;}
    }
}
