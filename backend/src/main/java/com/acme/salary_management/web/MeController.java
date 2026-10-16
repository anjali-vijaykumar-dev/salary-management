package com.acme.salary_management.web;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Tells the UI who is signed in and which roles they have, so it can hide actions they cannot perform. */
@RestController
public class MeController {

    public record CurrentUser(String name, List<String> roles) {
    }

    @GetMapping("/api/me")
    public CurrentUser me(Authentication authentication) {
        String name = authentication.getPrincipal() instanceof OidcUser oidcUser
                ? oidcUser.getEmail()
                : authentication.getName();
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring("ROLE_".length()))
                .sorted()
                .toList();
        return new CurrentUser(name, roles);
    }
}
