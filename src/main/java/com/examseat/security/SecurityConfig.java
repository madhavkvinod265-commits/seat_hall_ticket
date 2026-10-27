package com.examseat.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Static resources & Frontend
                .requestMatchers("/", "/index.html", "/login.html", "/lookup.html", "/dashboard.html", "/*.html",
                                 "/css/**", "/js/**", "/assets/**", "/favicon.ico", "/error").permitAll()
                
                // Public Authentication and Public Seat Lookup
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/lookup/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/exams/{examId}/tickets/{studentId}/pdf").permitAll() // Allowed for direct hall ticket download links

                // User Management and System Administration
                .requestMatchers("/api/users/**").hasRole("ADMINISTRATOR")
                .requestMatchers("/api/settings/**").hasRole("ADMINISTRATOR")

                // Halls management
                .requestMatchers(HttpMethod.GET, "/api/halls/**").hasAnyRole("ADMINISTRATOR", "EXAM_CELL_STAFF", "INVIGILATOR")
                .requestMatchers("/api/halls/**").hasAnyRole("ADMINISTRATOR", "EXAM_CELL_STAFF")

                // Student management & CSV Import
                .requestMatchers("/api/students/**").hasAnyRole("ADMINISTRATOR", "EXAM_CELL_STAFF")

                // Examination management
                .requestMatchers(HttpMethod.GET, "/api/exams/**").hasAnyRole("ADMINISTRATOR", "EXAM_CELL_STAFF", "INVIGILATOR")
                .requestMatchers("/api/exams/**").hasAnyRole("ADMINISTRATOR", "EXAM_CELL_STAFF")

                // Seating Plan generation & Finalization
                .requestMatchers(HttpMethod.GET, "/api/seating/**").hasAnyRole("ADMINISTRATOR", "EXAM_CELL_STAFF", "INVIGILATOR")
                .requestMatchers("/api/seating/**").hasAnyRole("ADMINISTRATOR", "EXAM_CELL_STAFF")

                // Invigilator reports
                .requestMatchers("/api/reports/**").hasAnyRole("ADMINISTRATOR", "EXAM_CELL_STAFF", "INVIGILATOR")

                // Hall tickets
                .requestMatchers("/api/tickets/**").authenticated()

                // Dashboard
                .requestMatchers("/api/dashboard/**").hasAnyRole("ADMINISTRATOR", "EXAM_CELL_STAFF", "INVIGILATOR")

                // Profile and Current User info
                .requestMatchers("/api/me/**").authenticated()

                // All other endpoints require authentication
                .anyRequest().authenticated()
            );

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
