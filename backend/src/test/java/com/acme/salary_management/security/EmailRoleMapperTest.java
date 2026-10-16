package com.acme.salary_management.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class EmailRoleMapperTest {

    private final EmailRoleMapper mapper =
            new EmailRoleMapper(Set.of("manager@acme.com"), Set.of("viewer@acme.com"));

    @Test
    void givesTheManagerRoleToAManagerEmailIgnoringCase() {
        assertThat(rolesFor("Manager@ACME.com", true)).contains("ROLE_HR_MANAGER").doesNotContain("ROLE_HR_VIEWER");
    }

    @Test
    void givesTheViewerRoleToAViewerEmail() {
        assertThat(rolesFor("viewer@acme.com", true)).contains("ROLE_HR_VIEWER").doesNotContain("ROLE_HR_MANAGER");
    }

    @Test
    void givesNoRoleToAnUnknownEmail() {
        assertThat(rolesFor("stranger@gmail.com", true)).doesNotContain("ROLE_HR_MANAGER", "ROLE_HR_VIEWER");
    }

    @Test
    void ignoresAnEmailThatIsNotVerified() {
        assertThat(rolesFor("manager@acme.com", false)).doesNotContain("ROLE_HR_MANAGER");
    }

    private List<String> rolesFor(String email, boolean verified) {
        OAuth2UserAuthority googleUser =
                new OAuth2UserAuthority(Map.of("email", email, "email_verified", verified), "email");
        Collection<? extends GrantedAuthority> mapped = mapper.mapAuthorities(List.of(googleUser));
        return mapped.stream().map(GrantedAuthority::getAuthority).toList();
    }
}
