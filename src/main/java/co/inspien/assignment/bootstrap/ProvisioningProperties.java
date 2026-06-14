package co.inspien.assignment.bootstrap;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * application-secret.yml 의 inspien.provisioning.* 값을 바인딩.
 */
@ConfigurationProperties(prefix = "inspien.provisioning")
public class ProvisioningProperties {

    private String url;
    private Auth auth = new Auth();
    private Applicant applicant = new Applicant();

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public Auth getAuth() { return auth; }
    public void setAuth(Auth auth) { this.auth = auth; }

    public Applicant getApplicant() { return applicant; }
    public void setApplicant(Applicant applicant) { this.applicant = applicant; }

    public static class Auth {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class Applicant {
        private String name;
        private String phoneNumber;
        private String email;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}
