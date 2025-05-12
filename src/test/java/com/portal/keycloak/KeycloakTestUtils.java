package com.portal.keycloak;

import com.portal.keycloak.config.KeycloakTestProperties;
import com.portal.keycloak.init.KeycloakContainerInitializer;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class KeycloakTestUtils {

    @Autowired
    protected KeycloakTestProperties keycloakProperties;

    /**
     * Get access token using client credentials
     * @param clientId Client ID
     * @param clientSecret Client Secret
     * @return Access token
     */
    public String getAccessToken(String clientId, String clientSecret) {

        String tokenUrl = KeycloakContainerInitializer.getKeycloakContainer().getAuthServerUrl()
                + "/realms/customer/protocol/openid-connect/token";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            return (String) response.getBody().get("access_token");
        } else {
            throw new RuntimeException("Failed to obtain access token: " + response);
        }
    }

    /**
     * Create a new user in Keycloak
     * @param username Username
     * @param password Password
     * @return boolean indicating success or failure
     */
    public boolean createUser(String email, String password, String firstName, String lastName) {

        KeycloakContainer keycloakContainer = KeycloakContainerInitializer.getKeycloakContainer();

        Keycloak keycloakClient = keycloakClient();

        AccessTokenResponse accessTokenResponse = keycloakClient.tokenManager().getAccessToken();
        String accessToken = accessTokenResponse.getToken();

        String userUrl = keycloakContainer.getAuthServerUrl() + "/admin/realms/customer/users";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        String userJson = String.format("{\"username\":\"%s\",\"email\":\"%s\",\"firstName\":\"%s\",\"lastName\":\"%s\",\"enabled\":true,\"credentials\":[{\"type\":\"password\",\"value\":\"%s\",\"temporary\":false}]}",
                email, email, firstName, lastName, password);
        HttpEntity<String> request = new HttpEntity<>(userJson, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(userUrl, request, String.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            return true;
        } else {
            throw new RuntimeException("Failed to create user: " + response);
        }
    }

    /**
     * Create a new group in Keycloak
     * @param groupName Group name
     * @return GroupRepresentation object
     */
    public GroupRepresentation createGroup(String groupName) {
        KeycloakContainer keycloakContainer = KeycloakContainerInitializer.getKeycloakContainer();

        Keycloak keycloakClient = keycloakClient();

        AccessTokenResponse accessTokenResponse = keycloakClient.tokenManager().getAccessToken();
        String accessToken = accessTokenResponse.getToken();

        String groupUrl = keycloakContainer.getAuthServerUrl() + "/admin/realms/customer/groups";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        String groupJson = String.format("{\"name\":\"%s\"}", groupName);
        HttpEntity<String> request = new HttpEntity<>(groupJson, headers);
        ResponseEntity<GroupRepresentation> response = restTemplate.postForEntity(groupUrl, request, GroupRepresentation.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            GroupRepresentation groupRepresentation = new GroupRepresentation();
            groupRepresentation.setId(response.getHeaders().getFirst("Location").split("/")[7]);
            return groupRepresentation;
        } else {
            throw new RuntimeException("Failed to create group: " + response);
        }
    }

    /**
     * Create Client Role
     * @param clientId Client ID
     * @param roleName Role name
     * @return boolean indicating success or failure
     */
    public boolean createClientRole(String clientId, String roleName) {
        KeycloakContainer keycloakContainer = KeycloakContainerInitializer.getKeycloakContainer();

        Keycloak keycloakClient = keycloakClient();

        AccessTokenResponse accessTokenResponse = keycloakClient.tokenManager().getAccessToken();
        String accessToken = accessTokenResponse.getToken();

        String roleUrl = keycloakContainer.getAuthServerUrl() + "/admin/realms/customer/clients/" + clientId + "/roles";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        String roleJson = String.format("{\"name\":\"%s\"}", roleName);
        HttpEntity<String> request = new HttpEntity<>(roleJson, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(roleUrl, request, String.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            return true;
        } else {
            throw new RuntimeException("Failed to create role: " + response);
        }
    }

    /**
     * Get client by name
     * @param clientName Client name
     * @return ClientRepresentation object
     */
    public ClientRepresentation getClientByName(String clientName){
        KeycloakContainer keycloakContainer = KeycloakContainerInitializer.getKeycloakContainer();

        Keycloak keycloakClient = keycloakClient();
        AccessTokenResponse accessTokenResponse = keycloakClient.tokenManager().getAccessToken();
        String accessToken = accessTokenResponse.getToken();
        String clientUrl = keycloakContainer.getAuthServerUrl() + "/admin/realms/customer/clients?clientId=" + clientName;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<ClientRepresentation[]> response = restTemplate.exchange(
                clientUrl,
                HttpMethod.GET,
                request,
                ClientRepresentation[].class
        );
        if (response.getStatusCode().is2xxSuccessful()) {
            ClientRepresentation[] clientRepresentations = response.getBody();
            if (clientRepresentations.length > 0) {
                return clientRepresentations[0];
            } else {
                throw new RuntimeException("Client not found: " + clientName);
            }
        } else {
            throw new RuntimeException("Failed to get client: " + response);
        }
    }

    /**
     * Get client roles
     * @param clientId Client ID
     * @return List of RoleRepresentation objects
     */
    public List<RoleRepresentation> getClientRoles(String clientId) {
        KeycloakContainer keycloakContainer = KeycloakContainerInitializer.getKeycloakContainer();

        Keycloak keycloakClient = keycloakClient();
        AccessTokenResponse accessTokenResponse = keycloakClient.tokenManager().getAccessToken();
        String accessToken = accessTokenResponse.getToken();
        String roleUrl = keycloakContainer.getAuthServerUrl() + "/admin/realms/customer/clients/" + clientId + "/roles";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<RoleRepresentation[]> response = restTemplate.exchange(
                roleUrl,
                HttpMethod.GET,
                request,
                RoleRepresentation[].class
        );
        if (response.getStatusCode().is2xxSuccessful()) {
            return Arrays.asList(response.getBody());
        } else {
            throw new RuntimeException("Failed to get roles: " + response);
        }
    }

    /**
     * Get Keycloak client instance
     * @return Keycloak client instance
     */
    private Keycloak keycloakClient(){
        KeycloakContainer keycloakContainer = KeycloakContainerInitializer.getKeycloakContainer();

        return Keycloak.getInstance(
            keycloakContainer.getAuthServerUrl(),
            "master",
            keycloakProperties.getAdmin().getUsername(),
            keycloakProperties.getAdmin().getPassword(),
            "admin-cli");
    }
}
