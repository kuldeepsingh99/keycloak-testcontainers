package com.portal.keycloak.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "application.keycloak")
public class KeycloakTestProperties {

    private String version;
    private Admin admin = new Admin();
    private Realm realm = new Realm();

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public Realm getRealm() {
        return realm;
    }

    public void setRealm(Realm realm) {
        this.realm = realm;
    }

    public static class Admin {
        private String username;
        private String password;

       public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }


    public static class Realm {
        private String name;
        private Client client = new Client();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Client getClient() {
            return client;
        }

        public void setClient(Client client) {
            this.client = client;
        }

        public static class Client {
            private String id;
            private String secret;

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getSecret() {
                return secret;
            }

            public void setSecret(String secret) {
                this.secret = secret;
            }
        }
    }
}
