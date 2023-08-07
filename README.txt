Dynamic file monitoring application and XML to JIRA issue application. Parses XML files containing contact data within a monitored file system and updates 
individual JIRA projects using the JIRA REST Java client and Java WatchService API. JIRA issues are populated with contact-specific custom fields that are
configured to be queried with JQL. Data visualizations of contact data points is handled by the Java JFreeChart library and visualizations are updated as 
the corresponding XML file is changed. 