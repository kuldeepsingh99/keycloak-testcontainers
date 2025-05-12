package com.portal.keycloak.init;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;
import java.io.IOException;
import java.util.Properties;

public class KeycloakContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static KeycloakContainer keycloakContainer;

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        try {
            Properties properties = loadYamlProperties("application-test.yml");

            String keycloakVersion = properties.getProperty("application.keycloak.version");
            String keycloakAdminUsername = properties.getProperty("application.keycloak.admin.username");
            String keycloakAdminPassword = properties.getProperty("application.keycloak.admin.password");
            String keycloakRealmName = properties.getProperty("application.keycloak.realm.name");

            // Start the Keycloak container
            keycloakContainer = new KeycloakContainer("quay.io/keycloak/keycloak:" + keycloakVersion)
                    .withEnv("KEYCLOAK_ADMIN", keycloakAdminUsername)
                    .withEnv("KEYCLOAK_ADMIN_PASSWORD", keycloakAdminPassword)
                    .withRealmImportFile("realm/" + keycloakRealmName + "-realm.json");

            keycloakContainer.start();

            // Dynamically set the properties in Spring's environment
            String issuerUri = keycloakContainer.getAuthServerUrl() + "/realms/" + keycloakRealmName;

            TestPropertyValues.of(
                    "keycloak.server.url=" + keycloakContainer.getAuthServerUrl(),
                    "spring.security.oauth2.resourceserver.jwt.issuer-uri=" + issuerUri
            ).applyTo(applicationContext.getEnvironment());

        } catch (IOException e) {
            throw new RuntimeException("Failed to load properties from YAML file", e);
        }
    }

    private Properties loadYamlProperties(String yamlFilePath) throws IOException {
        YamlPropertiesFactoryBean yamlPropertiesFactoryBean = new YamlPropertiesFactoryBean();
        yamlPropertiesFactoryBean.setResources(new ClassPathResource(yamlFilePath));
        return yamlPropertiesFactoryBean.getObject();
    }

    public static KeycloakContainer getKeycloakContainer() {
        return keycloakContainer;
    }
}
