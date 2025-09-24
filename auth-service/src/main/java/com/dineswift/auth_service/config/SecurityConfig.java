package com.dineswift.auth_service.config;


import com.dineswift.userservice.security.filter.JwtAuthenticationEntryPoint;
import com.dineswift.userservice.security.filter.JwtFilter;
import com.dineswift.userservice.security.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtFilter jwtFilter;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService, JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint, JwtFilter jwtFilter) {
        this.customUserDetailsService = customUserDetailsService;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf->csrf.disable())
                .cors(cors->cors.disable())
                .authorizeHttpRequests(auth->
                        auth.requestMatchers("user/sign-in","user/sign-up").permitAll().anyRequest().authenticated())
                .exceptionHandling(exception->exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(session->session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(){
        DaoAuthenticationProvider authenticationProvider=new DaoAuthenticationProvider(customUserDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(authenticationProvider);
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

}
