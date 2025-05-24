package id.ac.ui.cs.advprog.eventspherre.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(
        controllers = AdminAuditViewController.class,

        excludeFilters = @Filter(type = FilterType.REGEX,
                                 pattern = "id\\.ac\\.ui\\.cs\\.advprog\\.eventspherre\\.security\\..*"))
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AdminAuditViewControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /admin/audit → view 'admin/audit'")
    void auditAdminPage() throws Exception {
        mvc.perform(get("/admin/audit"))
           .andExpect(status().isOk())
           .andExpect(view().name("admin/audit"));
    }
}
