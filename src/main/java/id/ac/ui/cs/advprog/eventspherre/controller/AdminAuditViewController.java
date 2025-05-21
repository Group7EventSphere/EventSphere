package id.ac.ui.cs.advprog.eventspherre.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@PreAuthorize("hasRole('ADMIN')")
public class AdminAuditViewController {

    @GetMapping("/admin/audit_admin")
    public String auditPage() {
        return "admin_audit";
    }
}
