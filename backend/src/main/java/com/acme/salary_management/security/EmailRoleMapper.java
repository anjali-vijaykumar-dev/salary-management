package com.acme.salary_management.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Gives Google (SSO) users an application role based on their verified email address.
 * Users who are not on either list can sign in but get no role, so every salary endpoint returns 403.
 */
public class EmailRoleMapper implements GrantedAuthoritiesMapper {

    static final String MANAGER = "ROLE_HR_MANAGER";
    static final String VIEWER = "ROLE_HR_VIEWER";

    private final Set<String> managerEmails;
    private final Set<String> viewerEmails;

    public EmailRoleMapper(Set<String> managerEmails, Set<String> viewerEmails) {
        this.managerEmails = managerEmails;
        this.viewerEmails = viewerEmails;
    }

    @Override
    public Collection<? extends GrantedAuthority> mapAuthorities(Collection<? extends GrantedAuthority> authorities) {
        Set<GrantedAuthority> mapped = new HashSet<>(authorities);
        verifiedEmail(authorities).ifPresent(email -> {
            if (managerEmails.contains(email)) {
                mapped.add(new SimpleGrantedAuthority(MANAGER));
            } else if (viewerEmails.contains(email)) {
                mapped.add(new SimpleGrantedAuthority(VIEWER));
            }
        });
        return mapped;
    }

    private Optional<String> verifiedEmail(Collection<? extends GrantedAuthority> authorities) {
        return authorities.stream()
                .filter(OAuth2UserAuthority.class::isInstance)
                .map(OAuth2UserAuthority.class::cast)
                .filter(authority -> Boolean.TRUE.equals(authority.getAttributes().get("email_verified")))
                .map(authority -> authority.getAttributes().get("email"))
                .filter(String.class::isInstance)
                .map(email -> ((String) email).toLowerCase(Locale.ROOT))
                .findFirst();
    }
}
