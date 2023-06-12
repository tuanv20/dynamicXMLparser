package tuanv20.mockjiraapi;
import tuanv20.mockjiraapi.Model.Issue;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tuanv20.mockjiraapi.Model.Linechart;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import javax.xml.parsers.*;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

@SpringBootApplication
public class MockJiraApiApplication {
	public static final String ABS_PATH = "C:\\Users\\rebed\\Work\\mock-jira-api\\contact-gen";
    public static final String ARCHIVE_PATH = "C:\\\\Users\\\\rebed\\\\Work\\\\mock-jira-api\\contacts";
    public static final String[] dataTags = {"TIME", "DELTA_AZ", "DELTA_EL", "TLM_FR", "CMD"};
    
	public static void main (String[] args) throws IOException, InterruptedException {
		WatchService fileWatcher = FileSystems.getDefault().newWatchService();
        //Absolute path to the directory being watched
        Path directory = Paths.get(ABS_PATH);
        
        //Loads XML files that are currently in the directory 
        loadDirectory(ABS_PATH);

        //Subscribed the watchkey to the file creation event 
        directory.register((fileWatcher), StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
        WatchKey key;
        
        //Listen for events within the list of events returned by pollEvents()
        while( (key= fileWatcher.take()) != null){
            for ( WatchEvent<?> event: key.pollEvents()){
                String filename = event.context().toString();
                handleEvent(event, directory, filename);
            }
            //Reset the key to tell it to block and continue to wait for events to take 
            key.reset();
        }
		SpringApplication.run(MockJiraApiApplication.class, args);
	}
	public static void handleEvent(WatchEvent<?> event, Path directoryPath, String fileName) throws IOException{
        switch(event.kind().toString()){
            case "ENTRY_MODIFY":
                //Absolute path to file, path to directory, fileName
                modifyEvent(directoryPath.resolve( (Path) event.context()), directoryPath, fileName);
                break;
            case "ENTRY_DELETE":
                break;
            case "ENTRY_CREATE":
                modifyEvent(directoryPath.resolve( (Path) event.context()), directoryPath, fileName);
                break;
        }
    }

    /**
    * Helper method that reads in and stores the data from the 
    * directory being monitored on startup of the program
    *
    * @param  directory    Absolute path to directory being loaded represented as a String 
    *
    **/
    public static void loadDirectory(String directory){
        File path = new File(directory);
        File [] files = path.listFiles();
        for(File file: files){
            Path filePath = Paths.get(file.getAbsolutePath());
            Path directoryPath = Paths.get(directory);
            try{modifyEvent(filePath, directoryPath, file.getName());}
            catch(IOException e){e.printStackTrace();}
        }
    }

    /**
    * Helper method that handles the logic for a modification to a pre-existing 
    * file in the directory being monitored by the WatchService. Parses
    * the contact project ID and calls processDataNode() to handle the 
    * business logic of parsing children data nodes.
    *
    * @param  event                 The event that triggered the handler 
    * @param  directoryPath         The absolute path of the directory being monitored
    * @param  fileName              Name of the file that was modified 
    *
    **/

    public static void modifyEvent(Path filePath, Path directoryPath, String fileName) throws IOException{
        //Creates Issue (will eventually be added to collection of issues in database)
        Issue issue = new Issue();
        File outfile = null;
        //File and FileWriter creation (only for demo purposes) 
        //Will be replaced with data structure business logic 
        try{
            String outfilename = fileName.substring(0, fileName.lastIndexOf('.')) + "_results.txt";
            outfile = new File(outfilename);
            if (outfile.createNewFile()) {
                System.out.println("File created: " + outfile.getName());
            } 
            else {
                System.out.println("File already exists.");
            }
        } 
        catch (IOException e) {
            System.out.println("An error occurred.");
        }
        FileWriter writer = new FileWriter(outfile.getName());

        try{
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();  
            Document doc = db.parse(new File(filePath.toString()));

            //Element representing the root element 
            Element root = doc.getDocumentElement();

            //http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            root.normalize();

            //Retrieve root element and its attributes
            String rootString = root.getNodeName();
            writer.write("Root Element: " + rootString + "\n----------------------------------------------------\n");
            writer.write("Project ID: " + root.getAttribute("project_id") + "\n");
            
            //Iterates through children node of contact 
            NodeList dataList = root.getChildNodes();
            for (int i = 0; i < dataList.getLength(); i++) {
                Node dataNode = dataList.item(i);
                //Filters out empty text nodes and processes relevant nodes
                if(dataNode.getNodeType() == Node.ELEMENT_NODE){
                    processDataNode(dataNode, doc, writer, issue);
                }
            }
            writer.close();

            //Insert visualization logic here 
            Linechart linechart = new Linechart(fileName.substring(0, fileName.lastIndexOf('.')) + ".png", issue);
            linechart.exportAsPng();
            System.out.println("First Class: " + issue.FirstClass() + "\n");
            System.out.println("Parameters: " + issue.Params() + "\n");
            System.out.println("Data: " + issue.getData() + "\n");
            System.out.println(filePath.toString());
            System.out.println(ARCHIVE_PATH);
            Files.move(filePath, Paths.get(ARCHIVE_PATH + "\\" + fileName));

        }
        catch(ParserConfigurationException | SAXException | IOException e){
            e.printStackTrace();
        }

    }

    /**
    * Helper method that takes in a child data node of the root element of 
    * the XML file. Processes it according to 3 sub-types: PARAMS, DATA, and
    * first-class data elements. Params and Data nodes have subtags that need to 
    * be processed accordingly. 
    *
    * @param  node                  The node that is being processed 
    * @param  doc                   Overarching document object for the XML file
    * @param  writer                Filewrite object 
    *
    **/

    public static void processDataNode(Node node, Document doc, FileWriter writer, Issue issue) throws IOException{
        switch(node.getNodeName()){
            case "PARAMS":
                writer.write("----------------------------------------------------\n\nParams\n----------------------------------------------------\n");

                NodeList paramList = doc.getElementsByTagName("PARAM");
                //Loops through all elements matching PARAM tag name 
                for(int i = 0; i < paramList.getLength(); i++){
                    //Converts these Nodes back to elements to retrieve data from Name and Value subtags 
                    Element paramElement = (Element) paramList.item(i);
                    String name = paramElement.getElementsByTagName("NAME").item(0).getTextContent();
                    String value = paramElement.getElementsByTagName("VALUE").item(0).getTextContent();
                    issue.addParam(name, value);
                       writer.write(name + ": " + value + "\n");
                    }
                writer.write("----------------------------------------------------\n\n");
                break;

            case "DATA":
                writer.write("Data\n----------------------------------------------------\n");

                NodeList dataList = doc.getElementsByTagName("DATAPOINT");
                //Loops through all elements matching DATAPOINT tag name 
                for(int i = 0; i < dataList.getLength(); i++){
                    ArrayList<String> datapointVars = new ArrayList<String>();
                    //Converts these Nodes back to elements to retrieve relevant tag data
                    Element dataElement = (Element) dataList.item(i);
                    for(String dataTag : dataTags){
                        //Parse date to epoch if processing TIME tag
                        String value = dataElement.getElementsByTagName(dataTag).item(0).getTextContent();
                        //Convert value to epoch if processing time datatag
                        value = dataTag.equals("TIME") ? Long.toString(dateToEpoch(value)) : value;
                        writer.write(dataTag + ": " + value + "\n");  
                        datapointVars.add(value);
                    }
                    issue.addDataPoint(datapointVars);
                    writer.write("\n");
                }
                writer.write("----------------------------------------------------\n");
                break;
            
            //First-class data element 
            default:    
                String value = node.getNodeName() == "AOS" | node.getNodeName() == "LOS" ? Long.toString(dateToEpoch(node.getTextContent())) : node.getTextContent();
                issue.addFirstClass(node.getNodeName(), value);
                writer.write(node.getNodeName() + ": " + value + "\n");
                break;
        }
    }
    
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
    

