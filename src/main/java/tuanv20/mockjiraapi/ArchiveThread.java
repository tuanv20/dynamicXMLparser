package tuanv20.mockjiraapi;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
/** Thread that archives unmodified XMLs
 * ABS_DIR_PATH - Absolute path to contact-gen directory
 * ARCHIVE_PATH - Absolute path to archive directory
 * ARCHIVE_CHECK_TIME_SEC - How frequently archival occurs (sec)
 * MARKED_AS_ARCHIVE_TIME_SEC - When a file is marked as archived (sec)
*/
class ArchiveThread implements Runnable {
    private static Thread t;
    private String threadName;
    @Value("${paths.dir_path}")
    private String ABS_DIR_PATH;
    @Value("${paths.arch_path}")
    private String ARCHIVE_PATH;
    @Value("${arch.refresh_sec}")
    private long ARCHIVE_CHECK_TIME_SEC;
    @Value("${arch.mark_archive_time_sec}")
    private long MARKED_AS_ARCHIVE_TIME_SEC;

    @Autowired
    JIRALogger log;

    public ArchiveThread(){
        this.threadName = "Archive Thread";
    }

    public ArchiveThread(String name) {
        threadName = name;
    }

    /** Iterates through the contact-gen directory
     *  and compares the current time to the last 
     *  modified time of the XML file. Archives 
     *  any files that fall within the archival timeframe. 
    */
    public void archiveDirectory() {
        try {
            File path = new File(ABS_DIR_PATH);
            File[] files = path.listFiles();
            for(File file : files){
                Date last_modified_date = new Date(file.lastModified());
                Date curr_date = new Date();
                long millis_since_last_modification = curr_date.getTime() - last_modified_date.getTime();
                //log.info(file.getName() + ": " + Long.toString(millis_since_last_modification) + "ms");
                if(millis_since_last_modification > (MARKED_AS_ARCHIVE_TIME_SEC * 1000)){
                    Files.move(file.toPath(), Paths.get(ARCHIVE_PATH + "/" + file.getName()));
                    log.warn("Archiving file: " + file.getName());
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
            log.error("Error Archiving Files");
        }
    }
   
    public void run() {
        log.info("Running " +  threadName );
        try{
            while(true){
                Thread.sleep(ARCHIVE_CHECK_TIME_SEC * 1000);
                archiveDirectory();
            }
        }
        catch(InterruptedException e){
            e.printStackTrace();
            log.error("Archive Thread Interrupted");
        }
    }
    
    public void start () {
        log.info("Starting " +  threadName );
        if (t == null) {
            t = new Thread (this, threadName);
            t.setDaemon(true);
            t.start();
        }
    }
}
