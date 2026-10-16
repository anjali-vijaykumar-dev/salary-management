package com.acme.salary_management.security;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.util.StringUtils.hasText;

/**
 * Who can do what:
 * <ul>
 *   <li>HR_MANAGER – read and change salary data</li>
 *   <li>HR_VIEWER  – read only</li>
 *   <li>signed in without a role – can only see who they are (/api/me)</li>
 * </ul>
 * Sign-in: Google SSO (when configured) or demo username/password accounts for reviewers.
 */
@Configuration
@EnableConfigurationProperties(AppSecurityProperties.class)
public class SecurityConfig {

    static final String MANAGER = "HR_MANAGER";
    static final String VIEWER = "HR_VIEWER";

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            ObjectProvider<ClientRegistrationRepository> googleSso,
                                            GrantedAuthoritiesMapper emailRoleMapper) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/error").permitAll()
                        .requestMatchers("/api/me").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole(MANAGER, VIEWER)
                        .requestMatchers("/api/**").hasRole(MANAGER)
                        .anyRequest().authenticated())
                .formLogin(form -> form.defaultSuccessUrl("/", true).permitAll())
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler()))
                // API calls get a plain 401 (Angular redirects to /login); pages get the login redirect
                .exceptionHandling(ex -> ex.defaultAuthenticationEntryPointFor(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                        PathPatternRequestMatcher.withDefaults().matcher("/api/**")));

        // Google SSO is switched on only when a Google client is configured (environment variables),
        // so local development and tests work without Google credentials.
        if (googleSso.getIfAvailable() != null) {
            http.oauth2Login(oauth -> oauth
                    .defaultSuccessUrl("/", true)
                    .userInfoEndpoint(userInfo -> userInfo.userAuthoritiesMapper(emailRoleMapper)));
        }
        return http.build();
    }

    @Bean
    GrantedAuthoritiesMapper emailRoleMapper(AppSecurityProperties properties) {
        return new EmailRoleMapper(properties.managerEmails(), properties.viewerEmails());
    }

    @Bean
    UserDetailsService demoUsers(AppSecurityProperties properties, PasswordEncoder passwordEncoder) {
        List<UserDetails> users = new ArrayList<>();
        if (hasText(properties.demoManagerPassword())) {
            users.add(User.withUsername("hr.manager")
                    .password(passwordEncoder.encode(properties.demoManagerPassword()))
                    .roles(MANAGER)
                    .build());
        }
        if (hasText(properties.demoViewerPassword())) {
            users.add(User.withUsername("hr.viewer")
                    .password(passwordEncoder.encode(properties.demoViewerPassword()))
                    .roles(VIEWER)
                    .build());
        }
        return new InMemoryUserDetailsManager(users);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
