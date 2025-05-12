package com.portal.keycloak;

import com.portal.keycloak.config.KeycloakTestProperties;
import com.portal.keycloak.init.KeycloakContainerInitializer;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;



@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration(initializers = KeycloakContainerInitializer.class)
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseKeycloakTest {

    @Autowired
    protected KeycloakTestUtils keycloakTestUtils;


    @AfterAll
    static void tearDownKeycloak() {
        if (KeycloakContainerInitializer.getKeycloakContainer() != null) {
            KeycloakContainerInitializer.getKeycloakContainer().stop();
        }
    }
}
