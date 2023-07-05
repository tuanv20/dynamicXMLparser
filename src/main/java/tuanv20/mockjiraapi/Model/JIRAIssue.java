package tuanv20.mockjiraapi.Model;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import tuanv20.mockjiraapi.Controller.JiraController;

public class JIRAIssue {
    JiraController JiraREST = new JiraController();
    FirstClass firstClassElems;
    Param params;
    ArrayList<Data> data;
    ArrayList<String> events;
    String JIRAKey;
    String projKey;
    String id;
    ArrayList<File> attachments;

    public JIRAIssue(){
        this.firstClassElems = new FirstClass();
        this.params = new Param();
        this.data = new ArrayList<Data>();
        this.attachments = new ArrayList<File>();
        this.events = new ArrayList<String>();
    }

    public JIRAIssue(String JIRAKey){
        this.JIRAKey = JIRAKey;
    }
    
    public String getProjKey(){
        return this.projKey;
    }

    public String getJIRAKey(){
        return this.JIRAKey;
    }

    public String getID(){
        return this.id;
    }

    public ArrayList<File> getAttachments(){
        return this.attachments;
    }

    public URI getAttachmentsURI(){
        return JiraREST.getAttachmentsURI(this.JIRAKey);
    }

    public String createJIRAIssue(JIRAIssue issue, String filename){
        this.JIRAKey = JiraREST.createIssue(issue, filename);
        JiraREST.addAttachments(this.getAttachmentsURI(), getAttachments());
        JiraREST.addDescription(this.getJIRAKey(), this.eventDescription());
        return this.JIRAKey;
    }

    public void updateJIRAIssue(String JIRAKey) throws IOException{
        JiraREST.updateIssue(this, JIRAKey);
    }

    public void addAttachment(File attachment){
        this.attachments.add(attachment);
    }

    public void addDataPoint(ArrayList<String> vars){
        String[] dataPointVars = vars.toArray(new String[vars.size()]);
        this.data.add(new Data(dataPointVars));
    }

    public void setJIRAKey(String JIRAKey){
        this.JIRAKey = JIRAKey;
    }

    public void setProjKey(String projKey){
        this.projKey = projKey;
    }

    public void setID(String id){
        this.id = id;
    }

    public FirstClass getFirstClass(){
        return this.firstClassElems;
    }
     public Param getParams(){
        return this.params;
    }

     public ArrayList<Data> getData(){
        return this.data;
    }

    public ArrayList<String> getEvents(){
        return this.events;
    }

    public String eventDescription(){
        StringBuilder descBuilder = new StringBuilder();
        for(String event : this.events){
            descBuilder.append(event + "\\n");
        }
        return descBuilder.toString();
    }

    public String toString(){
        String out = "";
        out = "[" + firstClassElems.toString() + ", " + params.toString() + ", " + data.toString() + "]"; 
        return out;
    }  
}
