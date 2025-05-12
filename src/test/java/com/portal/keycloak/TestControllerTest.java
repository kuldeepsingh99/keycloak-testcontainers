package com.portal.keycloak;

import com.portal.keycloak.config.KeycloakTestProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class TestControllerTest extends BaseKeycloakTest {

    @Autowired
    protected KeycloakTestProperties keycloakProperties;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testProtectedEndpoint() {

        String accessToken = keycloakTestUtils.getAccessToken(
                keycloakProperties.getRealm().getClient().getId(),
                keycloakProperties.getRealm().getClient().getSecret());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange("/api/v2/products", HttpMethod.GET, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Hello Products");

    }
}
