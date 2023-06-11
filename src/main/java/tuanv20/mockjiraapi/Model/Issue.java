package tuanv20.mockjiraapi.Model;
import java.util.ArrayList;

public class Issue {
    int id; 
    long aos;
    ArrayList<FirstClass> firstClassElems;
    ArrayList<Param> params;
    ArrayList<Data> data;

    public Issue(){
        this.firstClassElems = new ArrayList<FirstClass>();
        this.params = new ArrayList<Param>();
        this.data = new ArrayList<Data>();
    }
    
    public void addParam(String name, String value){
        this.params.add(new Param(name, value));
    }

    public void addFirstClass(String name, String value){
        this.firstClassElems.add(new FirstClass(name, value));
    }

    public void addDataPoint(ArrayList<String> vars){
        String[] dataPointVars = vars.toArray(new String[vars.size()]);
        this.data.add(new Data(dataPointVars));
    }

    public ArrayList<FirstClass> getFirstClass(){
        return this.firstClassElems;
    }

     public ArrayList<Param> getParams(){
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
}
