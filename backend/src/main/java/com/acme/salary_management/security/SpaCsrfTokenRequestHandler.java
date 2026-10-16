package com.acme.salary_management.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.util.StringUtils;

import java.util.function.Supplier;

/**
 * CSRF handling for a single-page app (pattern from the Spring Security reference docs).
 * Angular reads the XSRF-TOKEN cookie and sends it back in the X-XSRF-TOKEN header;
 * the server-rendered login form sends it as a form field.
 */
final class SpaCsrfTokenRequestHandler implements CsrfTokenRequestHandler {

    private final CsrfTokenRequestHandler plain = new CsrfTokenRequestAttributeHandler();
    private final CsrfTokenRequestHandler xor = new XorCsrfTokenRequestAttributeHandler();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, Supplier<CsrfToken> csrfToken) {
        // BREACH protection for the token rendered into the login page
        this.xor.handle(request, response, csrfToken);
        // load the token on every request so the XSRF-TOKEN cookie is always present for Angular
        csrfToken.get();
    }

    @Override
    public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
        String headerValue = request.getHeader(csrfToken.getHeaderName());
        // header (Angular) carries the raw cookie value; form field (login page) carries the masked value
        return (StringUtils.hasText(headerValue) ? this.plain : this.xor).resolveCsrfTokenValue(request, csrfToken);
    }
}
