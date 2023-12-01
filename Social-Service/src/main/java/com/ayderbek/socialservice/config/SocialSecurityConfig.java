package com.ayderbek.socialservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SocialSecurityConfig {


    @Bean
    public SecurityFilterChain socialsecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeHttpRequests()
                .requestMatchers("/**")
                .hasAuthority("SCOPE_message.read")
                .and()
                .oauth2ResourceServer()
                .jwt();
        return http.build();
    }
}