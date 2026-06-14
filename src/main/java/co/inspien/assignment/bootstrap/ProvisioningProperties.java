package co.inspien.assignment.bootstrap;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "inspien.provisioning")
public class ProvisioningProperties {

    private String url;
    private Auth auth = new Auth();
    private Applicant applicant = new Applicant();

    @Getter
    @Setter
    public static class Auth {
        private String username;
        private String password;
    }

    @Getter
    @Setter
    public static class Applicant {
        private String name;
        private String phoneNumber;
        private String email;
    }
}
