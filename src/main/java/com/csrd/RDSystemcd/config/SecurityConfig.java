package com.csrd.RDSystemcd.config;

import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .cors(cors -> cors.configurationSource(request -> {
                CorsConfiguration config = new CorsConfiguration();
                config.addAllowedOrigin("http://localhost:5173");
                config.addAllowedHeader("*");
                config.addAllowedMethod("*");
                return config;
            }))

            .authorizeHttpRequests(auth -> auth

                .requestMatchers("/auth/login").permitAll()

                // 👑 SUPER ADMIN
                .requestMatchers("/auth/create-admin").hasAuthority("ROLE_SUPER_ADMIN")

                // ADMIN + SUPER ADMIN
                .requestMatchers(
                        "/rdusers/**",
                        "/psave",
                        "/pupdate/**",
                        "/pdelete/**",
                        "/passbook/**",
                        "/scheduler/**",
                        "/dashboard/**"
                ).hasAnyAuthority("ROLE_ADMIN", "ROLE_SUPER_ADMIN")

                .anyRequest().authenticated()
            )

            .addFilterBefore(jwtFilter,
                    UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}