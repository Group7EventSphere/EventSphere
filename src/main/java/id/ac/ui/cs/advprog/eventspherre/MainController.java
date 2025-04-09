package id.ac.ui.cs.advprog.eventspherre;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class MainController {
    
    @GetMapping("/")
    public String LoadMainPage() {
        // The string returned here is the logical name of the template view
        return "MainPage"; // Assuming you have a template file named "name.html" (or the appropriate extension for your template engine)
    }
    
}
