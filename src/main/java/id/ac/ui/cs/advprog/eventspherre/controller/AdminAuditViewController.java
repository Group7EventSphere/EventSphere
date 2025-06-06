package id.ac.ui.cs.advprog.eventspherre.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/admin")
public class AdminAuditViewController {

    @GetMapping("/audit")
    public String auditAdminPage() {
        return "admin/audit";
    }
}
