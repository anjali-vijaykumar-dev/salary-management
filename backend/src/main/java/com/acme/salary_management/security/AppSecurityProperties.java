package com.acme.salary_management.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Security settings, all supplied through environment variables so no secret is stored in the code.
 *
 * @param demoManagerPassword password for the "hr.manager" demo account (blank = account disabled)
 * @param demoViewerPassword  password for the "hr.viewer" demo account (blank = account disabled)
 * @param managerEmails       Google accounts that get the HR_MANAGER role
 * @param viewerEmails        Google accounts that get the HR_VIEWER role
 */
@ConfigurationProperties("app.security")
public record AppSecurityProperties(String demoManagerPassword,
                                    String demoViewerPassword,
                                    Set<String> managerEmails,
                                    Set<String> viewerEmails) {

    public AppSecurityProperties {
        managerEmails = normalize(managerEmails);
        viewerEmails = normalize(viewerEmails);
    }

    private static Set<String> normalize(Set<String> emails) {
        if (emails == null) {
            return Set.of();
        }
        return emails.stream()
                .map(String::trim)
                .filter(email -> !email.isEmpty())
                .map(email -> email.toLowerCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
    }
}
