package id.ac.ui.cs.advprog.eventspherre.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminAuditViewController {

    @GetMapping("/admin_audit")
    public String auditPage() {
        // resolves to src/main/resources/templates/admin_audit.html
        return "admin_audit";
    }
}
