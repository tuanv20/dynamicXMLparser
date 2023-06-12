package tuanv20.mockjiraapi.Model;
import java.util.ArrayList;
import java.util.HashMap;

public class Issue {
    FirstClassMap firstClassElems;
    HashMap<String, String> params;
    ArrayList<Data> data;

    public Issue(){
        this.firstClassElems = new FirstClassMap();
        this.params = new HashMap<String, String>();
        this.data = new ArrayList<Data>();
    }
    
    public void addParam(String name, String value){
        this.params.put(name, value);
    }

    public void addFirstClass(String name, String value){
        this.firstClassElems.add(name, value);
    }

    public void addDataPoint(ArrayList<String> vars){
        String[] dataPointVars = vars.toArray(new String[vars.size()]);
        this.data.add(new Data(dataPointVars));
    }

    public FirstClassMap FirstClass(){
        return this.firstClassElems;
    }
     public HashMap<String, String> Params(){
        return this.params;
    }

     public ArrayList<Data> getData(){
        return this.data;
    }

    public String toString(){
        String out = "";
        out = "[" + firstClassElems.toString() + ", " + params.toString() + ", " + data.toString() + "]"; 
        return out;
    }  

    public static class FirstClassMap{
        HashMap<String, String> map;

        public FirstClassMap(){
            map = new HashMap<String, String>();
        }

        public void add(String name, String value){
            this.map.put(name, value);
        }

        public String getID(){
            return map.get("ID");
        }

        public int getPN_H(){
            return Integer.parseInt(map.get("PN_H"));
        }

        public String getMP(){
            return map.get("MP");
        }

        public String getAntenna(){
            return map.get("ANTENNA");
        }

        public Long getAOS(){
            return Long.parseLong(map.get("AOS"));
        }

        public Long getLOS(){
            return Long.parseLong(map.get("LOS"));
        }

        public String toString(){
            String out = "[";
            for(String key : map.keySet()){
                out = out + key + ": " + map.get(key) + ", ";
            }
            out = out.substring(0, out.length() - 2);  
            out += "]";
            return out;
        }
    }
}
