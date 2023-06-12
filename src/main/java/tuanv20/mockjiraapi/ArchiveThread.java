package tuanv20.mockjiraapi;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

class ArchiveThread implements Runnable {
   private Thread t;
   private String threadName;
   
    ArchiveThread( String name) {
        threadName = name;
        System.out.println("Creating " +  threadName );
    }
   
    public void run() {
        System.out.println("Running " +  threadName );
        try {
            Thread.sleep(1000 * 60);
            Files.move(Paths.get("C:\\Users\\rebed\\Work\\mock-jira-api\\contact-gen\\contact1.xml"), Paths.get("C:\\Users\\rebed\\Work\\mock-jira-api\\contacts\\contact1.xml"));
            System.out.println("File moved");
        } 
        catch (InterruptedException | IOException e) {
            System.out.println("Thread " +  threadName + " interrupted.");
        }
        System.out.println("Thread " +  threadName + " exiting.");
    }
    
    public void start () {
        System.out.println("Starting " +  threadName );
        if (t == null) {
            t = new Thread (this, threadName);
            t.start ();
        }
    }
}
