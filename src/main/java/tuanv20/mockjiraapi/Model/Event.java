package tuanv20.mockjiraapi.Model;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Event {
    String name; 
    Long time; 

    public Event(String name, Long time){
        this.name = name;
        this.time = time; 
    }

    public String getName(){
        return this.name;
    }

    public Long getTime(){
        return this.time;
    }

    public String toString(){
        StringBuilder eventBuilder = new StringBuilder();
        eventBuilder.append(this.name);
        eventBuilder.append(" -- ");
        Date eventDate = new Date(this.time);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        eventBuilder.append(df.format(eventDate));
        return eventBuilder.toString();
    }

}
