package tuanv20.mockjiraapi;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.lang.System;

@Component
class ArchiveThread implements Runnable {
    private Thread t;
    private String threadName;
    @Value("${paths.dir_path}")
    private String ABS_DIR_PATH;
    @Value("${paths.arch_path}")
    private String ARCHIVE_PATH;
    @Value("${arch.refresh_sec}")
    private long ARCHIVE_CHECK_TIME_SEC;
    @Value("${arch.mark_archive_time_sec}")
    private long MARKED_AS_ARCHIVE_TIME_SEC;
    boolean main_thread_exit;

    public ArchiveThread(){
        this.threadName = "Archive Thread";
        System.out.println("Creating " +  threadName );
        this.main_thread_exit = false;
    }

    ArchiveThread(String name) {
        threadName = name;
    }
   
    public void run() {
        System.out.println("Running " +  threadName );
        try {
            while(main_thread_exit == false){
                Thread.sleep(ARCHIVE_CHECK_TIME_SEC * 1000);
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
