package tuanv20.mockjiraapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.nio.file.FileSystems;
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
import java.util.Date;

@SpringBootApplication
public class MockJiraApiApplication {
	public static final String ABS_PATH = "C:\\Users\\rebed\\Work\\mock-jira-api\\contact-gen";
    public static final String[] paramTags = {"NAME", "VALUE"};
    public static final String[] dataTags = {"TIME", "DELTA_AZ", "DELTA_EL", "TLM_FR", "CMD"};

	public static void main(String[] args) throws IOException, InterruptedException{
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
                    processDataNode(dataNode, doc, writer);
                }
            }
            writer.close();
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

    public static void processDataNode(Node node, Document doc, FileWriter writer) throws IOException{
        switch(node.getNodeName()){
            case "PARAMS":
                writer.write("\nParams\n----------------------------------------------------\n\n");

                NodeList paramList = doc.getElementsByTagName("PARAM");
                //Loops through all elements matching PARAM tag name 
                for(int i = 0; i < paramList.getLength(); i++){
                    //Converts these Nodes back to elements to retrieve data from Name and Value subtags 
                    Node paramNode = paramList.item(i);
                    Element paramElement = (Element) paramNode;
                    for(String paramTag : paramTags){
                       writer.write("\n" + paramTag + ": " + paramElement.getElementsByTagName(paramTag).item(0).getTextContent());
                    }
                    writer.write("\n\n");
                }
                writer.write("----------------------------------------------------\n\n");
                break;

            case "DATA":
                writer.write("Data\n----------------------------------------------------\n");

                NodeList dataList = doc.getElementsByTagName("DATAPOINT");
                //Loops through all elements matching DATAPOINT tag name 
                for(int i = 0; i < dataList.getLength(); i++){
                    //Converts these Nodes back to elements to retrieve relevant tag data
                    Node dataNode = dataList.item(i);
                    Element dataElement = (Element) dataNode;
                    for(String dataTag : dataTags){
                        //Parse date to epoch if processing TIME tag
                        if(dataTag.equals("TIME")){
                            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String contactTime = dataElement.getElementsByTagName("TIME").item(0).getTextContent();
                            try{
                                Date contactDate = df.parse(contactTime);
                                long epoch = contactDate.getTime();
                                writer.write("TIME: " + epoch);
                            }
                            catch(ParseException e){e.printStackTrace();}
                        }
                        else{
                       //Grabs the value of the tag specified by dataTag
                            writer.write("\n" + dataTag + ": " + dataElement.getElementsByTagName(dataTag).item(0).getTextContent());
                        }
                    }
                    writer.write("\n\n");
                }
                writer.write("----------------------------------------------------\n");
                break;
            
            //First-class data element 
            default:    
                writer.write("Node " + node.getNodeName() + ": " + node.getTextContent() + "\n");
                break;
        }
    }
}
