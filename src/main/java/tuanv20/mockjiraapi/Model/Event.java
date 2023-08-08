package tuanv20.mockjiraapi.Model;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Event {
    String name; 
    Long date; 

    public Event(String name, Long date){
        this.name = name;
        this.date = date; 
    }

    public String getName(){
        return this.name;
    }

    public String getDate(){
        Date eventDate = new Date(this.date);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return df.format(eventDate);
    }
}
