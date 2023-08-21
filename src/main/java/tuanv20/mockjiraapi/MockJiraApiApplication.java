package tuanv20.mockjiraapi;
import tuanv20.mockjiraapi.Controller.JiraController;
import tuanv20.mockjiraapi.Model.JIRAIssue;
import tuanv20.mockjiraapi.Model.XMLParser;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService; 
import java.util.Date;
import java.io.*;


@SpringBootApplication
public class MockJiraApiApplication {
	public static String ABS_PATH;
    public static String ARCHIVE_PATH;
    public static long ARCHIVE_TIME;
    public static ArchiveThread archThread;
    public static JiraController JIRAController;
    public static XMLParser parser;
    public static ConfigurableApplicationContext appContext;
    
	public static void main (String[] args) throws IOException, InterruptedException {
        appContext = SpringApplication.run(MockJiraApiApplication.class, args);
        ABS_PATH = appContext.getEnvironment().getProperty("paths.dir_path");
        ARCHIVE_PATH = appContext.getEnvironment().getProperty("paths.arch_path");
        ARCHIVE_TIME = Long.parseLong(appContext.getEnvironment().getProperty("arch.mark_archive_time_sec"));
        archThread = appContext.getBean(ArchiveThread.class);     
        parser = appContext.getBean(XMLParser.class);
        JIRAController = appContext.getBean(JiraController.class); 
        JIRAController.getAllFields();
        archThread.start();
		WatchService fileWatcher = FileSystems.getDefault().newWatchService();
        Path directory = Paths.get(ABS_PATH);
        directory.register((fileWatcher), StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
        WatchKey key;

        
        //Listen for events within the list of events returned by pollEvents()
        while( (key=fileWatcher.take()) != null){
            for ( WatchEvent<?> event: key.pollEvents()){
                String filename = event.context().toString();
                handleEvent(event, directory, filename);
            }
            key.reset();
        }
    }
	public static void handleEvent(WatchEvent<?> event, Path directoryPath, String fileName) throws IOException{
        switch(event.kind().toString()){
            case "ENTRY_MODIFY":
                modifyEvent(directoryPath.resolve( (Path) event.context()), fileName);
                break;
            case "ENTRY_DELETE":
                deleteEvent(fileName);
                break;
            case "ENTRY_CREATE":
                modifyEvent(directoryPath.resolve( (Path) event.context()), fileName);
                break;
        }
    }

    /**
    * Helper method that reads in and stores the data from the 
    * directory being monitored on startup of the program
    *
    * @param  directory    Absolute path to directory being loaded represented as a String 
    *
    */

    public static void loadDirectory(String directory){
        File path = new File(directory);
        File [] files = path.listFiles();
        for(File file: files){
            Path filePath = Paths.get(file.getAbsolutePath());
            modifyEvent(filePath, file.getName());
        }
    }

    /**
    * Helper method that handles the logic for a modification to a pre-existing 
    * file in the directory being monitored by the WatchService. Parses
    * the contact project ID and calls processDataNode() to handle the 
    * business logic of parsing children data nodes.
    *
    * @param  filePath              The path to the file that was modified
    * @param  fileName              Name of the file that was modified 
    *
    **/

    public static void modifyEvent(Path filePath, String fileName) {
        //Creates Issue (will eventually be added to collection of issues in database)
        Date last_modified_date = new Date(filePath.toFile().lastModified());
        Date curr_date = new Date();
        long millis_since_last_modification = curr_date.getTime() - last_modified_date.getTime();
        //Only process files that have not been marked as archivable
        if(millis_since_last_modification < (ARCHIVE_TIME * 1000)){
            JIRAIssue issue = new JIRAIssue(JIRAController);
            parser.parseXML(issue, filePath, fileName, JIRAController);
        }
    }

    /**
    * Helper method that handles the logic for the deletion of a
    * file in the directory being monitored by the WatchService. 
    * Utilizes the JIRA REST Controller to find the JIRA issue by 
    * filename and remove it from the JIRA instance. 
    *
    * @param  fileName              Name of the file to be deleted
    *
    **/
    public static void deleteEvent(String fileName){
        JIRAController.deleteIssueByFileName(fileName);
    }
}

    

