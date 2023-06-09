package tuanv20.mockjiraapi.Model;
import java.util.ArrayList;

public class Issue {
    ArrayList<FirstClass> firstClassElems;
    ArrayList<Param> params;
    ArrayList<Data> data;

    public Issue(){
        this.firstClassElems = new ArrayList<FirstClass>();
        this.params = new ArrayList<Param>();
        this.data = new ArrayList<Data>();
    }

    public void addParam(Param addParam){
        this.params.add(addParam);
    }

    public void addFirstClass(String name, String value){
        this.firstClassElems.add(new FirstClass(name, value));
    }

    public String toString(){
        String out = "";
        out = "[" + firstClassElems.toString() + ", " + params.toString() + ", " + data.toString() + "]"; 
        return out;
    }  
}
