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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RestController;
import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.domain.Attachment;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;

import com.atlassian.jira.rest.client.api.domain.input.LinkIssuesInput;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import tuanv20.mockjiraapi.JIRALogger;
import tuanv20.mockjiraapi.Model.JIRAIssue;

@RestController
/** JIRA REST API Controller
 * JIRA REST Java Client Core Maven Dependency 
 * Bearer Token Authentication Handler
 * HTTPURLConnection for CRUD Operations 
*/
public class JiraController {
    private static final Map<String, String> CUSTOMFIELDS = Map.of(
        "Filename", "customfield_10104",
        "AOS", "customfield_10106",
        "LOS", "customfield_10107",
        "Antenna", "customfield_10200",
        "PN-H", "customfield_10109",
        "Progress", "customfield_10201",
        "H-EQUIP", "customfield_10111",
        "H-CONFIG", "customfield_10112",
        "L-CONFIG", "customfield_10113",
        "MP", "customfield_10110"
    );

    @Value("${jira.url}")
    private String JIRA_URL;
    @Value("${jira.auth}")
    private String AUTH_HEADER;
    @Value("${jira.main_proj_key}")
    private String mainJiraKey;
    private BearerHttpAuthenticationHandler JIRAHandler = new BearerHttpAuthenticationHandler();
    private JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
    private JiraRestClient restClient;
    private IssueRestClient issueClient;
    private SearchRestClient searchClient;

    @Autowired
    JIRALogger log;

    @Autowired
    public JiraController(@Value("${jira.url}") String url){
        this.restClient = factory.create(URI.create(url), JIRAHandler);
        this.issueClient = restClient.getIssueClient();
        this.searchClient = restClient.getSearchClient();
    }

    public String getIssue(String issue_ID){
        return issueClient.getIssue(issue_ID).claim().toString();
    }

    public String createIssue(JIRAIssue issue, String filename){
        long issueTypeID = 10100;
        IssueInputBuilder newIssueBuilder = new IssueInputBuilder(issue.getProjKey(), issueTypeID, "Contact " + issue.getID());
        newIssueBuilder.setFieldValue(CUSTOMFIELDS.get("Filename"), filename);
        newIssueBuilder.setFieldValue(CUSTOMFIELDS.get("AOS"), issue.getFirstClass().getAOSCustom());
        newIssueBuilder.setFieldValue(CUSTOMFIELDS.get("LOS"), issue.getFirstClass().getLOSCustom());
        newIssueBuilder.setFieldValue(CUSTOMFIELDS.get("Antenna"), ComplexIssueInputFieldValue.with("value", issue.getFirstClass().getAntenna()));
        newIssueBuilder.setFieldValue(CUSTOMFIELDS.get("PN-H"), issue.getFirstClass().getPN_H());   
        newIssueBuilder.setFieldValue(CUSTOMFIELDS.get("MP"), issue.getFirstClass().getMP());   
        newIssueBuilder.setFieldValue(CUSTOMFIELDS.get("H-EQUIP"), issue.getParams().getHEQUIP());
        newIssueBuilder.setFieldValue(CUSTOMFIELDS.get("H-CONFIG"), issue.getParams().getHCONFIG());
        newIssueBuilder.setFieldValue(CUSTOMFIELDS.get("L-CONFIG"), issue.getParams().getLCONFIG());
        newIssueBuilder.setFieldValue(CUSTOMFIELDS.get("Progress"), ComplexIssueInputFieldValue.with("value", "New"));
        IssueInput newIssue = newIssueBuilder.build();
        String issueKey = issueClient.createIssue(newIssue).claim().getKey();
        log.info("Successfully Created JIRA issue: " + issueKey);
        return issueKey;
    }

    public void updateIssue(JIRAIssue issue, String JIRAKey, String mainIssueJiraKey){
        deleteAllAttachments(JIRAKey);
        deleteAllAttachments(mainIssueJiraKey);
        URI mainAttachmentsUri = getAttachmentsURI(mainIssueJiraKey);
        addAttachments(issue.getAttachmentsURI(), mainAttachmentsUri, issue.getAttachments());
        long issueTypeID = 10100;
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
        log.info("Successfully Updated JIRA issue: " + JIRAKey);
        
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
        URL url = new URL(JIRA_URL + "/rest/api/2/attachment/" + attachmentID);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setRequestMethod("DELETE");
        httpConn.setRequestProperty("Authorization", AUTH_HEADER);
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
        URL url = new URL(JIRA_URL + "/rest/api/2/issue/" + issue_ID + "/properties/info");
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setRequestMethod("PUT");
        httpConn.setRequestProperty("Content-type", "application/json");
        httpConn.setRequestProperty("Authorization", AUTH_HEADER);
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
        URL url = new URL(JIRA_URL + "/rest/api/2/issue/" + issueKey);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setRequestMethod("PUT");

        httpConn.setRequestProperty("Content-type", "application/json");
        httpConn.setRequestProperty("Authorization", AUTH_HEADER);

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

    public void addLabel(String issueKey, String label){
        try{
        URL url = new URL(JIRA_URL + "/rest/api/2/issue/" + issueKey);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setRequestMethod("PUT");

        httpConn.setRequestProperty("Content-type", "application/json");
        httpConn.setRequestProperty("Authorization", AUTH_HEADER);

        httpConn.setDoOutput(true);
        OutputStreamWriter writer = new OutputStreamWriter(httpConn.getOutputStream());
        writer.write("{\"update\":{\"labels\":" + "[{\"add\": " + "\"" + label + "\"}]}}");
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
        for(Issue issue: issues){
            issueClient.deleteIssue(issue.getKey(), true);
        }
    }

    public String createMainIssue(JIRAIssue issue, String filename){
        long issueTypeID = 10100;
        IssueInputBuilder newIssueBuilder = new IssueInputBuilder(mainJiraKey, issueTypeID, "Contact " + issue.getID());
        newIssueBuilder.setFieldValue(CUSTOMFIELDS.get("Filename"), filename);
        newIssueBuilder.setFieldValue(CUSTOMFIELDS.get("AOS"), issue.getFirstClass().getAOSCustom());
        newIssueBuilder.setFieldValue(CUSTOMFIELDS.get("LOS"), issue.getFirstClass().getLOSCustom());
        newIssueBuilder.setFieldValue(CUSTOMFIELDS.get("Antenna"), ComplexIssueInputFieldValue.with("value", issue.getFirstClass().getAntenna())); 
        newIssueBuilder.setFieldValue(CUSTOMFIELDS.get("PN-H"), issue.getFirstClass().getPN_H());
        newIssueBuilder.setFieldValue(CUSTOMFIELDS.get("MP"), issue.getFirstClass().getMP());
        newIssueBuilder.setFieldValue(CUSTOMFIELDS.get("H-EQUIP"), issue.getParams().getHEQUIP());
        newIssueBuilder.setFieldValue(CUSTOMFIELDS.get("H-CONFIG"), issue.getParams().getHCONFIG());
        newIssueBuilder.setFieldValue(CUSTOMFIELDS.get("L-CONFIG"), issue.getParams().getLCONFIG());
        newIssueBuilder.setFieldValue(CUSTOMFIELDS.get("Progress"), ComplexIssueInputFieldValue.with("value", "New"));
        IssueInput newIssue = newIssueBuilder.build();
        String issueKey = issueClient.createIssue(newIssue).claim().getKey();
        log.info("Successfully Created JIRA issue: " + issueKey);
        return issueKey;

    }

    public void linkIssues(String mainIssueKey, String linkedIssueKey){
        LinkIssuesInput linkInput = new LinkIssuesInput(mainIssueKey, linkedIssueKey, "Duplicate");
        issueClient.linkIssue(linkInput);    
        }

    public void setAntenna(String projKey){
        try{
            URL url = new URL("http://vmoc-proj1.nrl.navy.mil:8080/rest/api/2/issue/");
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setRequestMethod("PUT");

            httpConn.setRequestProperty("Content-type", "application/json");
            httpConn.setRequestProperty("Authorization", "Bearer OTY5Njk2Mzg2NzA4Oh0amfT2mfNUjmNrlL/zR0uRroQr");

            httpConn.setDoOutput(true);
            OutputStreamWriter writer = new OutputStreamWriter(httpConn.getOutputStream());
            writer.write("{\"fields\": {\n       \"project\":\n       {\n          \"key\":" + projKey +  "\n       },\n       \"customfield_10200\": { \"value\": \"DGS-A\" }");
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
}
