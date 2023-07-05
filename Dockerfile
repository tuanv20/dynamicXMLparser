# Fetching latest version of Java
FROM openjdk:18

# Setting up work directory
WORKDIR /app

# Copy the jar file into our app
COPY ./target/mock-jira-api-0.0.1-SNAPSHOT.jar /app
COPY ./contact-gen /app/contact-gen
COPY ./contacts /app/contacts
COPY ./contact-png /app/contact-png
COPY scriptjira.py /app/scriptjira.py

# Exposing port 8080
EXPOSE 8080

# Starting the application
CMD ["java", "-jar", "mock-jira-api-0.0.1-SNAPSHOT.jar"]