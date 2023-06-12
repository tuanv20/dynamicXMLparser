package tuanv20.mockjiraapi;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Date;

import org.springframework.boot.autoconfigure.couchbase.CouchbaseProperties.Io;

import ch.qos.logback.core.net.SyslogOutputStream;

import java.lang.System;

class ArchiveThread implements Runnable {
    private Thread t;
    private String threadName;
    public static final String ABS_DIR_PATH = "C:\\Users\\rebed\\Work\\mock-jira-api\\contact-gen";
    public static final String ARCHIVE_PATH = "C:\\Users\\rebed\\Work\\mock-jira-api\\contacts";
    public static final long ARCHIVE_CHECK_TIME_SEC = 5;
    public static final long MARKED_AS_ARCHIVE_TIME_SEC = 30;
   boolean main_thread_exit;
   
    ArchiveThread(String name) {
        threadName = name;
        System.out.println("Creating " +  threadName );
        this.main_thread_exit = false;

    }
   
    public void run() {
        System.out.println("Running " +  threadName );
        try {
            while(main_thread_exit == false){
                File path = new File(ABS_DIR_PATH);
                File[] files = path.listFiles();
                for(File file : files){
                    Date last_modified_date = new Date(file.lastModified());
                    Date curr_date = new Date();
                    long millis_since_last_modification = curr_date.getTime() - last_modified_date.getTime();
                    System.out.println(millis_since_last_modification);
                    if(millis_since_last_modification > (MARKED_AS_ARCHIVE_TIME_SEC * 1000)){
                        Files.move(file.toPath(), Paths.get(ARCHIVE_PATH + "\\" + file.getName()));
                    System.out.println("File moved");
                    }
                }
                Thread.sleep(ARCHIVE_CHECK_TIME_SEC * 1000);
            }
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
            t.setDaemon(true);
            t.start ();
        }
    }

    public void signal_stop() {
        main_thread_exit = true;
    }
}
