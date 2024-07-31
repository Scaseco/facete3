package org.aksw.facete3.app.vaadin;

import java.util.stream.Stream;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.SecurityFilterChain;

import com.vaadin.flow.server.HandlerHelper;
import com.vaadin.flow.shared.ApplicationConstants;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Configures Spring Security, doing the following:
 * <li>Bypass security checks for static resources,</li>
 * <li>Restrict access to the application, allowing only logged in users,</li>
 * <li>Set up the login form,</li>
 */
// @EnableWebSecurity
@Configuration
public class ConfigSecurity { // extends WebSecurityConfigurerAdapter {

    private static final String LOGIN_URL = "/login";
    private static final String LOGOUT_URL = "/logout";
    private static final String LOGOUT_SUCCESS_URL = "/";

    /**
     * Registers our UserDetailsService and the password encoder to be used on
     * login attempts.
     */
    //@Override
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return
        // @formatter:off
        http
            .authorizeHttpRequests(requests -> requests
                .requestMatchers(ConfigSecurity::isFrameworkInternalRequest).permitAll()
                .anyRequest().permitAll()
                //.anyRequest().authenticated()
            )
            // Allow all flow internal requests.
            // .authorizeRequests().requestMatchers(ConfigSecurity::isFrameworkInternalRequest).permitAll()

            // Restrict access to our application.
            // .authorizeRequests().anyRequest().permitAll()
            // .and().authorizeRequests().anyRequest().authenticated()

            // Not using Spring CSRF here to be able to use plain HTML for the login page
            .csrf(csrf -> csrf.disable())

            // Configure logout
            .logout(logout -> logout.logoutUrl(LOGOUT_URL).logoutSuccessUrl(LOGOUT_SUCCESS_URL))

            // Configure the login page.
            .oauth2Login(login -> login.loginPage(LOGIN_URL).permitAll())
            .build();
        // @formatter:on
    }

    /**
     * Allows access to static resources, bypassing Spring Security.
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers(
                // client-side JS code
                "/VAADIN/**",

                // the standard favicon URI
                "/favicon.ico",

                // web application manifest
                "/manifest.webmanifest", "/sw.js", "/offline-page.html",

                // icons and images
                "/icons/**", "/images/**");
    }

    /**
     * Tests if the request is an internal framework request. The test consists
     * of checking if the request parameter is present and if its value is
     * consistent with any of the request types know.
     *
     * @param request
     *            {@link HttpServletRequest}
     * @return true if is an internal framework request. False otherwise.
     */
    public static boolean isFrameworkInternalRequest(HttpServletRequest request) {
        String parameterValue = request.getParameter(ApplicationConstants.REQUEST_TYPE_PARAMETER);

        return parameterValue != null
                && Stream.of(HandlerHelper.RequestType.values()).anyMatch(
                        r -> r.getIdentifier().equals(parameterValue));
    }
}
