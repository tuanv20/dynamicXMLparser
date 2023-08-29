package tuanv20.mockjiraapi.Model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class FirstClass {
    HashMap<String, String> map;

    public FirstClass() {
        map = new HashMap<String, String>();
    }

    public void add(String name, String value) {
        this.map.put(name, value);
    }

    public String getID() {
        return map.get("ID");
    }

    public String getPN_H() {
        return map.get("PN_H");
    }

    public String getMP() {
        return map.get("MP");
    }

    public String getAntenna() {
        return map.get("ANTENNA");
    }

    public Long getAOS() {
        return Long.parseLong(map.get("AOS"));
    }

    public String getAOSCustom() {
        Date date = new Date(this.getAOS());
        SimpleDateFormat df = new SimpleDateFormat("YYYY-MM-dd'T'HH:mm:ss.SSS-0600");
        return df.format(date);
    }

    public Long getLOS() {
        return Long.parseLong(map.get("LOS"));
    }

    public String getLOSCustom() {
        Date date = new Date(this.getLOS());
        SimpleDateFormat df = new SimpleDateFormat("YYYY-MM-dd'T'HH:mm:ss.SSS-0600");
        return df.format(date);
    }

    public String toString() {
        String out = "[";
        for (String key : map.keySet()) {
            out = out + key + ": " + map.get(key) + ", ";
        }
        out = out.substring(0, out.length() - 2);
        out += "]";
        return out;
    }
}