package co.inspien.assignment.bootstrap;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "inspien.provisioning")
public record ProvisioningProperties(
        String url,
        Auth auth,
        Applicant applicant
) {
    public record Auth(String username, String password) {}

    public record Applicant(String name, String phoneNumber, String email) {}
}
