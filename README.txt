Dynamic file monitoring application and XML to JIRA issue application. Parses XML files containing contact data within a monitored file system and updates 
individual JIRA projects using the JIRA REST Java client and Java WatchService API. JIRA issues are populated with contact-specific custom fields that are
configured to be queried with JQL. Data visualizations of contact data points is handled by the Java JFreeChart library and visualizations are updated as 
the corresponding XML file is changed. 

Usage:
In the mock-jira-api directory, run the `mvn spring-boot:run` command. The XMLgen.py script can be ran to generate a random XML data file with
random representative test data. The -n option can be used to specify a number of files to generate (> 1). Any files that are dragged into the contact-gen
directory will have a representative JIRA issue for them and will update dynamically. The project is multi-threaded and contains an archival thread 
that removes the XML files from the contact-gen directory. The frequency of the archival thread and the denotation for how old a file has to be to be archived 
is all specified in the application properties of the project.

Environment Variables:
-The paths of the relevant directories are specified in order to support integration into a different container environment
-Archival frequency and how long a file needs to be untouched before it's archived
-URL of JIRA instance, auth token, project key of the main project 

JIRA Specifics:
-If starting from scratch with a brand new JIRA instance you'll need to configure the custom fields manually. Make sure you add them to the correct screens 
for each individual project. The ones that I created are listed under CUSTOMFIELDS in the JIRAController. The customfield IDs are generated when you create 
a custom field so depending on the order you create them they may not match the ones specified identically. You can find the ID of the field after creating it 
by following this guide: https://confluence.atlassian.com/jirakb/how-to-find-any-custom-field-s-ids-744522503.html.4

-Any XML file will create two representative JIRA issues. One will be in the project specified in the data file itself and another one will be in the project
whose key is specified by the main project key environment variable. The project JIRA issue will be linked to the main JIRA issue. (Make sure the set of project
keys in the XMLGen.py file matches your JIRA instance's project keys if you use that for testing/development).

-The token is a PAT (personal access token) generated through JIRA and is needed to manipulate any of the issues/data within the JIRA instance. Specifying a valid
token in the environment variables for your specific JIRA instance (specified by the URL in the environment variables) should be all you need to do.

Visualizations: 
I used the Java JFreeChart library in order to visualize the Telemetry frames over time for the test XML data files. Due to not having a representative data set, I 
can't say with full confidence whether or not this library has the all of the desired functionality for the project nor did I have a chance to explore that. I found 
it very difficult to find a Java visualization library that could compete with the likes of D3.js or Matplotlib (Python) but that may be something to explore.

Additional Notes: 
-I experimented with JIRA webhooks to be able receive notifications when specific actions on the JIRA instance were occuring and generating responses within the backend
of the project but couldn't get that working. Definitely something worth looking into.
-Keith and some of the other people that I demoed to seemed to really like the builtin JIRA widgets for visualizations. The application currently adds project labels to 
each JIRA issue which can be used to sort them and display them on these widgets. This is something that would need to be added manually if starting from scratch. 



