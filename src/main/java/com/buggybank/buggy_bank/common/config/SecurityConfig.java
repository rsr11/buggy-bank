package com.buggybank.buggy_bank.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF protects browser sessions/cookies. We'll use stateless tokens, so turn it off.
            .csrf(csrf -> csrf.disable())

            // Never create an HTTP session; every request must carry its own credentials.
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth
                // Public for now: health check, Swagger, and the temporary ping endpoint
                .requestMatchers(
                    "/actuator/health/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                  //  "/api/ping/**",
                    "/api/auth/**"
                ).permitAll()
                // Everything else needs authentication
                .anyRequest().authenticated()
            )

            // Temporary login method; Milestone 3 replaces this with JWT
            // .httpBasic(Customizer.withDefaults());
              .oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults()));

        return http.build();
    }

      @Bean
      PasswordEncoder passwordEncoder() {
      return new BCryptPasswordEncoder();
  }
}