package com.test.assignment_2.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  UserDetailsService userDetailsService(PasswordEncoder encoder) {
    UserDetails admin = User.withUsername("admin")
            .password(encoder.encode("admin123"))
            .roles("ADMIN")
            .build();

    return new InMemoryUserDetailsManager(admin);
  }

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(
                            "/swagger-ui/**",
                            "/v3/api-docs/**",
                            "/api/public/**",

                            // ✅ CHO ADMIN XEM KHÔNG LOGIN
                            "/api/admin/**"
                    ).permitAll()

                    .anyRequest().permitAll()
            )
            .httpBasic(basic -> basic.disable())   // ❌ tắt luôn basic auth
            .formLogin(form -> form.disable());    // ❌ tắt login page

    return http.build();
  }

}

