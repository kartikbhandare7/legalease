package com.legalease.config;

import com.legalease.common.security.JwtAuthFilter;
import com.legalease.auth.service.OAuth2UserService;
import com.legalease.common.security.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity           // enables @PreAuthorize on controller methods
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final OAuth2UserService oAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final UserDetailsServiceImpl userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF — we are stateless JWT, no session cookies
                .csrf(AbstractHttpConfigurer::disable)

                // CORS config — allow React frontend origin
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Session is STATELESS — JWT handles auth, no server-side session
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth

                        // ── PUBLIC routes — no token needed (delayed auth pattern) ──
                        .requestMatchers(
                                "/",
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/auth/refresh",
                                "/oauth2/**",
                                "/login/oauth2/**",
                                "/api/public/**"
                        ).permitAll()

                        // ── ADMIN only ──
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // ── LAWYER only — AI log, PDF export, case creation ──
                        .requestMatchers(
                                "/api/ai/**",
                                "/api/docs/export-pdf",
                                "/api/cases",
                                "/api/clients/intake"
                        ).hasRole("LAWYER")

                        // ── LAWYER + CLERK — read and log ──
                        .requestMatchers(
                                "/api/cases/**",
                                "/api/clients/**",
                                "/api/hearings/**",
                                "/api/dashboard/**"
                        ).hasAnyRole("LAWYER", "CLERK")

                        // Everything else needs authentication
                        .anyRequest().authenticated()
                )

                // Google OAuth2 login configuration
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo ->
                                userInfo.userService(oAuth2UserService))  // our custom service
                        .successHandler(oAuth2SuccessHandler)         // issues JWT after Google login
                )

                // Our JWT filter runs before Spring's default username/password filter
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // BCrypt with strength 12 — strong enough for production
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
//        provider.setUserDetailsService();
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // CORS — allow only your React frontend origin
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // In prod replace with your actual frontend domain
        config.setAllowedOrigins(List.of(
                "http://localhost:5173",   // Vite dev server
                "https://yourdomain.com"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));

        // Allow Authorization header in responses — needed for JWT
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}