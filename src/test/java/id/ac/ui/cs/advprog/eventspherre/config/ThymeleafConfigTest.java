package id.ac.ui.cs.advprog.eventspherre.config;

import org.junit.jupiter.api.Test;
import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.assertj.core.api.Assertions.assertThat;

class ThymeleafConfigTest {

    @Test
    void springSecurityDialect_shouldReturnSpringSecurityDialectInstance() {
        // Arrange
        ThymeleafConfig config = new ThymeleafConfig();
        
        // Act
        SpringSecurityDialect dialect = config.springSecurityDialect();
        
        // Assert
        assertNotNull(dialect);
        assertThat(dialect).isInstanceOf(SpringSecurityDialect.class);
    }
    
    @Test
    void springSecurityDialect_shouldHaveCorrectDialectPrefix() {
        // Arrange
        ThymeleafConfig config = new ThymeleafConfig();
        
        // Act
        SpringSecurityDialect dialect = config.springSecurityDialect();
        
        // Assert
        assertThat(dialect.getPrefix()).isEqualTo("sec");
    }
}