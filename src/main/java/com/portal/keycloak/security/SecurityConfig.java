package com.portal.keycloak.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collection;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    public String issuerUri;

    @Value("${application.client}")
    public String clientName;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        http
                .cors(Customizer.withDefaults());

        // Allow unauthenticated access to /api/v2/customers
        http.authorizeHttpRequests((authorize) -> authorize
                .requestMatchers("/api/v2/customers").permitAll()
                .anyRequest().authenticated());

        http.oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                        .decoder(JwtDecoders.fromIssuerLocation(issuerUri))
                        .jwtAuthenticationConverter(customJwtAuthenticationConverter())));

        http.sessionManagement(sessionAuthenticationStrategy -> sessionAuthenticationStrategy
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter customJwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(customJwtGrantedAuthoritiesConverter());
        return converter;
    }

    @Bean
    public Converter<Jwt, Collection<GrantedAuthority>> customJwtGrantedAuthoritiesConverter() {
        return new CustomJwtGrantedAuthoritiesConverter(clientName);
    }

    @Bean
    protected CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", new CorsConfiguration().applyPermitDefaultValues());
        return source;
    }
}
