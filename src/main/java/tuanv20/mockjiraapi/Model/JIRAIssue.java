package tuanv20.mockjiraapi.Model;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;

import tuanv20.mockjiraapi.Controller.JiraController;
import tuanv20.mockjiraapi.JIRALogger;

public class JIRAIssue {
    JiraController JiraREST = new JiraController();
    FirstClass firstClassElems;
    Param params;
    ArrayList<Data> data;
    ArrayList<Event> events;
    String mainIssueJiraKey;
    String JIRAKey;
    String projKey;
    String id;
    ArrayList<File> attachments;

    @Autowired
    JIRALogger log;

    public JIRAIssue(){
        this.firstClassElems = new FirstClass();
        this.params = new Param();
        this.data = new ArrayList<Data>();
        this.attachments = new ArrayList<File>();
        this.events = new ArrayList<Event>();
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
        this.mainIssueJiraKey = JiraREST.createMainIssue(issue, filename);
        JiraREST.addLabel(this.mainIssueJiraKey, this.projKey);
        this.JIRAKey = JiraREST.createIssue(issue, filename);
        JiraREST.linkIssues(this.JIRAKey, this.mainIssueJiraKey);
        URI mainAttachmentsURI = JiraREST.getAttachmentsURI(this.mainIssueJiraKey);
        JiraREST.addAttachments(this.getAttachmentsURI(), mainAttachmentsURI, getAttachments());
        JiraREST.addDescription(this.mainIssueJiraKey, this.eventDescription());
        JiraREST.addDescription(this.JIRAKey, this.eventDescription());
        return this.JIRAKey;
    }

    public void updateJIRAIssue(String JIRAKey, String mainIssueJiraKey) throws IOException{
        JiraREST.updateIssue(this, JIRAKey, mainIssueJiraKey);
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

    public void setMainIssueKey(String mainIssueJiraKey){
        this.mainIssueJiraKey = mainIssueJiraKey;
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

    public ArrayList<Event> getEvents(){
        return this.events;
    }

    public String eventDescription(){
        StringBuilder descBuilder = new StringBuilder();
        descBuilder.append("||Event Name||Event Date||\\n");
        for(Event event : this.events){
            descBuilder.append("|" + event.getName() + "|" + event.getDate() + "|\\n");
        }
        return descBuilder.toString();
    }

    public String toString(){
        String out = "";
        out = "[" + firstClassElems.toString() + ", " + params.toString() + ", " + data.toString() + "]"; 
        return out;
    }  
}
