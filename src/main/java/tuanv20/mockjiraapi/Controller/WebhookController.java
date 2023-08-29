package tuanv20.mockjiraapi.Controller;

// import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebhookController {
    @PostMapping("/webhook")
    public void handleWebhook() {
        // Handle the incoming webhook request here
        System.out.println("Issue Created!");
    }
}