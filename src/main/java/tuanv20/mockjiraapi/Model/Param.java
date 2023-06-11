package tuanv20.mockjiraapi.Model;

public class Param {
    String name;
    String value;

    public Param(String name, String value){
        this.name = name;
        this.value = value; 
    }

    public String getName(){
        return this.name;
    }

    public void setName(String name){
        this.name = name;
    }

     public String getValue(){
        return this.value;
    }

    public void setValue(String value){
        this.value = value;
    }

    public String toString(){
        return "[" + this.name + ": " + this.value + "]";
    }
}
