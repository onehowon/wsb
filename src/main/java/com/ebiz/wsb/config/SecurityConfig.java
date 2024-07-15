package com.ebiz.wsb.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable() // CSRF 비활성화
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/users/register").permitAll() // requestMatchers로 변경
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginProcessingUrl("/api/login")
                        .permitAll())
                .logout(logout -> logout.permitAll());
        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/api/users/register"); // antMatchers 대신 requestMatchers 사용
    }
}
