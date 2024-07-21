package com.example.demo.core;

import com.example.demo.user.service.jwt.JwtAuthorizationTokenFilter;
import com.example.demo.user.service.jwt.JwtTokenProvider;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import lombok.AllArgsConstructor;
import org.springframework.security.config.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
@AllArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        http.cors(Customizer.withDefaults());
        http.httpBasic(AbstractHttpConfigurer::disable);
        http.formLogin(AbstractHttpConfigurer::disable);
        http.authorizeHttpRequests(authorize -> authorize
//                .requestMatchers("/user/join").permitAll()
//                .requestMatchers("/**").permitAll()
//                .requestMatchers("/login").permitAll()
//                .requestMatchers("/user/**").permitAll()
//                .requestMatchers("/attendance/**").permitAll()
                        .anyRequest().permitAll()
        );

        http.sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(
                SessionCreationPolicy.STATELESS));

        http.addFilterBefore(new JwtAuthorizationTokenFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);




        return http.build();
    }



}
