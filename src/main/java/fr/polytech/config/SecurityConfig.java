package fr.polytech.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthConverter jwtAuthConverter = new JwtAuthConverter();

    /**
     * Configure the security filter chain to intercept all requests
     * @param http HttpSecurity object to configure
     * @return SecurityFilterChain to be used by Spring Security
     * @throws Exception if an error occurs while configuring the HttpSecurity object
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/api/v1/user/auth/login").permitAll()
                        .requestMatchers("/api/v1/user/auth/register").permitAll()
                        .anyRequest().authenticated());

        http
                .oauth2ResourceServer((oauth2) -> oauth2
                        .jwt((jwt) -> jwt
                                .jwtAuthenticationConverter(jwtAuthConverter)
                        )
                );

        http
                .sessionManagement((session) -> session.sessionCreationPolicy(STATELESS));

        return http.build();
    }
}