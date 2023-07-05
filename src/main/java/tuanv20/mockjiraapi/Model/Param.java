package tuanv20.mockjiraapi.Model;
import java.util.HashMap;

public  class Param{
    HashMap<String, String> map;

    public Param(){
        this.map = new HashMap<String, String>();
    }

    public void add(String name, String value){
        map.put(name, value);
    }

    public String getHEQUIP(){
        return map.get("H-EQUIP");
    }

    public String getHCONFIG(){
        return map.get("H-CONFIG");
    }

    public String getLCONFIG(){
        return map.get("L-CONFIG");
    }
}
