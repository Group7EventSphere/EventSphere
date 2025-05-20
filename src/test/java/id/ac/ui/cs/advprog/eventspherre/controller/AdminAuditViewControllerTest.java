package id.ac.ui.cs.advprog.eventspherre.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminAuditViewController.class)
class AdminAuditViewControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void auditPage_returnsAdminAuditView() throws Exception {
        mvc.perform(get("/admin_audit"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin_audit"));
    }
}
