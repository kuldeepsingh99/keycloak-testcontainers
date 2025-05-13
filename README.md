# 🔐 Spring Boot Integration Testing with Keycloak Testcontainers

## Keycloak Testcontainer: What Is It and Why Use It?
Keycloak Testcontainer is a great tool for performing integration tests involving Keycloak (or other authentication systems) in your application. It's a Testcontainer for Keycloak that allows you to spin up a real Keycloak server inside a Docker container during your tests. This is useful when you want to perform real integration tests that mimic how Keycloak would behave in production, but without the need to manually set up and tear down the Keycloak instance.

### Why Use Keycloak Testcontainer?
- **Realistic Testing**
  - Since you are using an actual Keycloak server (instead of mocking it), you’re testing with the same configuration that would run in production.

- **Automated Setup and Teardown:**
  - You don’t need to manually manage a Keycloak instance. Testcontainers automatically handles starting and stopping the Keycloak server for each test run.

- **Isolation:**
  - Keycloak runs in a separate container, so tests are isolated from your local environment. This helps ensure consistency across different environments.

- **Easy to Integrate:**

  - You can easily use it in your Spring Boot integration tests, and it can be automatically started and stopped within the test lifecycle.
 

This repository demonstrates how to perform integration testing in a Spring Boot application that uses Keycloak for authentication, by leveraging Keycloak Testcontainers.
---

✅ Features:
- Spring Boot application with secured endpoints

- Integration tests using real Keycloak instance spun up via Testcontainers

- Automatic JWT token generation for testing secured routes

- Example of Keycloak realm import and admin token usage

- Comparison-friendly setup for different JWT testing approaches

🧪 What You'll Learn:
- How to write integration tests for secured endpoints

- How to use Keycloak Testcontainers for automated, isolated auth testing

- How to simulate real-world authentication in tests without mocking

## Explanatation of the code

Here we have one Controller where endpoints are secured

```
@RestController
@RequestMapping("/api/v2")
public class TestController {


    @GetMapping("/customers")
    public String getCustomers() {
        return "Hello Customers";
    }

    @PreAuthorize("hasAuthority('products:read')")
    @GetMapping("/products")
    public String getProducts() {
        return "Hello Products";
    }
}
```

Now to perform integration testing we need a valid JWT Token and keycloak must be running because Spring Security will validate the token with keycloak, we have a issuer url in the JWT Token

### 🚀 Keycload TestContainer Initilization

Check this keycloak testcontainer initilization file [KeycloakContainerInitializer.java](https://github.com/kuldeepsingh99/keycloak-testcontainers/blob/main/src/test/java/com/portal/keycloak/init/KeycloakContainerInitializer.java)

```
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
```
**Important things to note in this file**

- We are reading the configuration like keycloak version, admin credentials from property file.
- we are setting the isser uri dynamically
- we are exposing the KeycloakContainer to be used in other classes
- we are importing a realm with a client, please check this [file](https://github.com/kuldeepsingh99/keycloak-testcontainers/blob/main/src/test/resources/realm/customer-realm.json)

### 🚀 Property file

In the test yml file most of the things are configurable

```
application:
  keycloak:
    version: 25.0.2
    admin:
      username: admin
      password: admin@123
    realm:
      name: customer
      client:
        id: "department"
        secret: "**********"
```

### 🚀 build.grade file

```
testImplementation 'com.github.dasniko:testcontainers-keycloak:3.7.0'
```
This is the important dependency that is required for keycloak testcontaines

### 🚀 Realm and Client Import JSON

This a file where we have realm and client details, Please check this [customer-realm.json](https://github.com/kuldeepsingh99/keycloak-testcontainers/blob/main/src/test/resources/realm/customer-realm.json)

👉 Best way to create this file is to export this file from Realm Setting, it will have all the necessary clients and required roles for your service to work

![image](https://github.com/user-attachments/assets/0054bb4b-9999-4be0-b15e-abe52e59097d)

👉 The other option is to create a keycloak environment with docker and create a realm and client 

```
docker run -p 8080:8080 -e KC_BOOTSTRAP_ADMIN_USERNAME=admin -e KC_BOOTSTRAP_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:26.2.4 start-dev
```

### 🚀 Keycloak Utility class

Check this [KeycloakTestUtils](https://github.com/kuldeepsingh99/keycloak-testcontainers/blob/main/src/test/java/com/portal/keycloak/KeycloakTestUtils.java) where i have created few utility methods like **createAccessToken**, **createUser**, **createGroup**, **createClientRole**, **getClientByName**.

One more point to note here is that to perform any admin operation we need a Access Token, so we are generating the **master** realm access token, please check `keycloakClient` method

As per your requirement you can create methods and play 💥 with keycloak 

```
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
```

### 🚀 Final Test Class

We have only one test where we generate **client-credentials** access token and execute the REST Endpoint with valid access token

```
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
```

### Final Conclusion
This is just an example to demonstrate how to configure keycloak testcontainer, as per your project structure classes can be restructured.

If you wants to know more about keycloak please refer this [keycloak Series](https://www.youtube.com/playlist?list=PLm2o_WHhxh2ioDeOdigAHPZ_SyOFpUygq)





