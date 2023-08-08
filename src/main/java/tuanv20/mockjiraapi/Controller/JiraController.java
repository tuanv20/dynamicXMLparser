package tuanv20.mockjiraapi.Controller;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.domain.Attachment;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.api.domain.input.LinkIssuesInput;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import tuanv20.mockjiraapi.JIRALogger;
import tuanv20.mockjiraapi.Model.JIRAIssue;

@RestController
public class JiraController {
    private static final Map<String, String> CUSTOMFIELDS = Map.of(
        "Filename", "customfield_10104",
        "AOS", "customfield_10106",
        "LOS", "customfield_10107",
        "Antenna", "customfield_10108",
        "PN-H", "customfield_10109",
        "MP", "customfield_10110",
        "H-EQUIP", "customfield_10111",
        "H-CONFIG", "customfield_10112",
        "L-CONFIG", "customfield_10113"
    );
    private static BearerHttpAuthenticationHandler JIRAHandler = new BearerHttpAuthenticationHandler();
    private static JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
    private static JiraRestClient restClient = factory.create(URI.create("http://vmoc-proj1.nrl.navy.mil:8080"), JIRAHandler);
    private static IssueRestClient issueClient = restClient.getIssueClient();
    private static SearchRestClient searchClient = restClient.getSearchClient();
    private static String mainJiraKey = "MAIN";

    @Autowired
    JIRALogger log;

    public String getIssue(String issue_ID){
        return issueClient.getIssue(issue_ID).claim().toString();
    }

    public String createIssue(JIRAIssue issue, String filename){
        long issueTypeID = 10000;
        IssueInputBuilder newIssueBuilder = new IssueInputBuilder(issue.getProjKey(), issueTypeID, "Contact " + issue.getID());
        newIssueBuilder.setFieldValue(CUSTOMFIELDS.get("Filename"), filename);
        newIssueBuilder.setFieldValue(CUSTOMFIELDS.get("AOS"), issue.getFirstClass().getAOSCustom());
        newIssueBuilder.setFieldValue(CUSTOMFIELDS.get("LOS"), issue.getFirstClass().getLOSCustom());
        newIssueBuilder.setFieldValue(CUSTOMFIELDS.get("Antenna"), issue.getFirstClass().getAntenna());
        newIssueBuilder.setFieldValue(CUSTOMFIELDS.get("PN-H"), issue.getFirstClass().getPN_H());
        newIssueBuilder.setFieldValue(CUSTOMFIELDS.get("MP"), issue.getFirstClass().getMP());
        newIssueBuilder.setFieldValue(CUSTOMFIELDS.get("H-EQUIP"), issue.getParams().getHEQUIP());
        newIssueBuilder.setFieldValue(CUSTOMFIELDS.get("H-CONFIG"), issue.getParams().getHCONFIG());
        newIssueBuilder.setFieldValue(CUSTOMFIELDS.get("L-CONFIG"), issue.getParams().getLCONFIG());
        IssueInput newIssue = newIssueBuilder.build();
        return issueClient.createIssue(newIssue).claim().getKey();
    }

    public void updateIssue(JIRAIssue issue, String JIRAKey, String mainIssueJiraKey){
        deleteAllAttachments(JIRAKey);
        deleteAllAttachments(mainIssueJiraKey);
        URI mainAttachmentsUri = getAttachmentsURI(mainIssueJiraKey);
        addAttachments(issue.getAttachmentsURI(), mainAttachmentsUri, issue.getAttachments());
        long issueTypeID = 10000;
        IssueInputBuilder newIssueBuilder = new IssueInputBuilder(issue.getProjKey(), issueTypeID, "Contact " + issue.getID());
        newIssueBuilder.setFieldValue(CUSTOMFIELDS.get("AOS"), issue.getFirstClass().getAOSCustom());
        newIssueBuilder.setFieldValue(CUSTOMFIELDS.get("LOS"), issue.getFirstClass().getLOSCustom());
        newIssueBuilder.setFieldValue(CUSTOMFIELDS.get("Antenna"), issue.getFirstClass().getAntenna());
        newIssueBuilder.setFieldValue(CUSTOMFIELDS.get("PN-H"), issue.getFirstClass().getPN_H());
        newIssueBuilder.setFieldValue(CUSTOMFIELDS.get("MP"), issue.getFirstClass().getMP());
        newIssueBuilder.setFieldValue(CUSTOMFIELDS.get("H-EQUIP"), issue.getParams().getHEQUIP());
        newIssueBuilder.setFieldValue(CUSTOMFIELDS.get("H-CONFIG"), issue.getParams().getHCONFIG());
        newIssueBuilder.setFieldValue(CUSTOMFIELDS.get("L-CONFIG"), issue.getParams().getLCONFIG());
        IssueInput newIssue = newIssueBuilder.build();
        issueClient.updateIssue(JIRAKey, newIssue);
        addDescription(JIRAKey, issue.eventDescription());
    }

    public URI getAttachmentsURI(String issue_ID){
        return issueClient.getIssue(issue_ID).claim().getAttachmentsUri();
    }

    public void addAttachments(URI linkedAttachmentURI, URI mainAttachmentURI, ArrayList<File> attachments){
        for(File attachment : attachments){
            issueClient.addAttachments(linkedAttachmentURI, attachment);
            issueClient.addAttachments(mainAttachmentURI, attachment);
            attachment.delete();
        }
    }

    public void deleteAttachment(String attachmentID) throws IOException{
        URL url = new URL("http://vmoc-proj1.nrl.navy.mil:8080/rest/api/2/attachment/" + attachmentID);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setRequestMethod("DELETE");
        httpConn.setRequestProperty("Authorization", "Bearer OTY5Njk2Mzg2NzA4Oh0amfT2mfNUjmNrlL/zR0uRroQr");
        httpConn.setRequestProperty("Accept", "application/json");
        httpConn.getResponseCode();
    }

    public ArrayList<String> getAttachmentIds(String issue_ID){
        Iterable<Attachment> attachments = issueClient.getIssue(issue_ID).claim().getAttachments();
        ArrayList<String> AttachmentIDs = new ArrayList<String>();
        for(Attachment attachment: attachments){
            String[] URI = attachment.getContentUri().toString().split("/");
            AttachmentIDs.add(URI[5]);
        }
        return AttachmentIDs;
    }

    public void deleteAllAttachments(String issue_ID) {
        try{
            System.out.println(issue_ID);
            ArrayList<String> attachmentIDs = this.getAttachmentIds(issue_ID);
            for(String attachmentID: attachmentIDs){
                deleteAttachment(attachmentID);
            }
        }
        catch(IOException e){
            e.printStackTrace();
            log.error("Error Deleting All Attachments");
        }
    }

    public boolean issueExists(String contactID){
        SearchResult issueSearch = searchClient.searchJql("summary ~ " + contactID).claim();
        boolean exists = issueSearch.getTotal() == 0 ? false : true;
        return exists;
    }

    public Iterable<Issue> getIssueKeyByContactID(String contactID){
        Iterable<Issue> issues = searchClient.searchJql("summary ~ " + contactID).claim().getIssues();
        return issues;
    }

    public void addFilenameProperty(String issue_ID, String fileName) {
        try{
        URL url = new URL("http://vmoc-proj1.nrl.navy.mil:8080/rest/api/2/issue/" + issue_ID + "/properties/info");
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setRequestMethod("PUT");
        httpConn.setRequestProperty("Content-type", "application/json");
        httpConn.setRequestProperty("Authorization", "Bearer OTY5Njk2Mzg2NzA4Oh0amfT2mfNUjmNrlL/zR0uRroQr");
        httpConn.setDoOutput(true);
        OutputStreamWriter writer = new OutputStreamWriter(httpConn.getOutputStream());
        writer.write("{\"Name\": " + "\"" + fileName + "\"}");
        writer.flush();
        writer.close();
        httpConn.getOutputStream().close();
        httpConn.getResponseCode();
        }
        catch(IOException e){
            e.printStackTrace();
            System.out.println("Error Adding Filename Property");
        }
    }

    public Iterable<Issue> getAllIssues(){
        return searchClient.searchJql("").claim().getIssues();
    }

    public void getAllFields(){
        Iterable<Issue> issues = getAllIssues();
        for(Issue issue: issues){
            System.out.println(issue.getFields());
        }
    }

    public void deleteAllIssues(){
        Iterable<Issue> issues = getAllIssues();
        for(Issue issue : issues){
            issueClient.deleteIssue(issue.getKey(), true);
        }
    }

    public void addDescription(String issueKey, String descString){
        try{
        URL url = new URL("http://vmoc-proj1.nrl.navy.mil:8080/rest/api/2/issue/" + issueKey);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setRequestMethod("PUT");

        httpConn.setRequestProperty("Content-type", "application/json");
        httpConn.setRequestProperty("Authorization", "Bearer OTY5Njk2Mzg2NzA4Oh0amfT2mfNUjmNrlL/zR0uRroQr");

        httpConn.setDoOutput(true);
        OutputStreamWriter writer = new OutputStreamWriter(httpConn.getOutputStream());
        writer.write("{\"fields\":{\"description\":" + "\"" + descString + "\"}}");
        writer.flush();
        writer.close();
        httpConn.getOutputStream().close();
        httpConn.getResponseCode();
        }
        catch(IOException e){
            e.printStackTrace();
            System.err.println("Error adding description");
        }
    }

    public void deleteIssueByFileName(String filename){
        Iterable<Issue> issues = searchClient.searchJql("Filename ~ " + filename).claim().getIssues();
        Issue delIssue = null;
        for(Issue issue : issues){
            delIssue = issue;
        }
        issueClient.deleteIssue(delIssue.getKey(), true);
    }

    public String createMainIssue(JIRAIssue issue, String filename){
        long issueTypeID = 10000;
        IssueInputBuilder newIssueBuilder = new IssueInputBuilder(mainJiraKey, issueTypeID, "Contact " + issue.getID());
        newIssueBuilder.setFieldValue(CUSTOMFIELDS.get("Filename"), filename);
        newIssueBuilder.setFieldValue(CUSTOMFIELDS.get("AOS"), issue.getFirstClass().getAOSCustom());
        newIssueBuilder.setFieldValue(CUSTOMFIELDS.get("LOS"), issue.getFirstClass().getLOSCustom());
        newIssueBuilder.setFieldValue(CUSTOMFIELDS.get("Antenna"), issue.getFirstClass().getAntenna());
        newIssueBuilder.setFieldValue(CUSTOMFIELDS.get("PN-H"), issue.getFirstClass().getPN_H());
        newIssueBuilder.setFieldValue(CUSTOMFIELDS.get("MP"), issue.getFirstClass().getMP());
        newIssueBuilder.setFieldValue(CUSTOMFIELDS.get("H-EQUIP"), issue.getParams().getHEQUIP());
        newIssueBuilder.setFieldValue(CUSTOMFIELDS.get("H-CONFIG"), issue.getParams().getHCONFIG());
        newIssueBuilder.setFieldValue(CUSTOMFIELDS.get("L-CONFIG"), issue.getParams().getLCONFIG());
        IssueInput newIssue = newIssueBuilder.build();
        return issueClient.createIssue(newIssue).claim().getKey();
    }

    public void linkIssues(String mainIssueKey, String linkedIssueKey){
        LinkIssuesInput linkInput = new LinkIssuesInput(mainIssueKey, linkedIssueKey, "Duplicate");
        issueClient.linkIssue(linkInput);    
        }

    // public void archiveIssueByFileName(String filename){
    // Iterable<Issue> issues = searchClient.searchJql("Filename ~" + filename).claim().getIssues();
    // Issue archIssue = null;
    // for(Issue issue : issues){
        
    // }
    // }
}
