package tuanv20.mockjiraapi.Controller;

import tuanv20.mockjiraapi.Model.Issue;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class MockJiraApiController {
    @PostMapping(value="/addIssue", consumes=MediaType.APPLICATION_JSON_VALUE)   
    public ResponseEntity<?> addTask(@RequestBody Issue issue){
        Issue addTask = new Issue();
        return new ResponseEntity<String>("This is a test", null);
    }
}
