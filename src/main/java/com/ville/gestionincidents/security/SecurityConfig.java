package com.ville.gestionincidents.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/auth/login", "/auth/register", "/auth/verify", "/static/css/**", "/static/js/**", "/css/**", "/js/**", "/images/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("/auth/login")
                .loginProcessingUrl("/auth/login")
                .defaultSuccessUrl("/home", true)
               // .failureUrl("/auth/login?error=true")
                .failureHandler((request, response, exception) -> {

                    System.out.println("=================================");
                    System.out.println("❌ LOGIN FAILED");
                    System.out.println("Type : " + exception.getClass().getSimpleName());
                    System.out.println("Message : " + exception.getMessage());
                    System.out.println("=================================");

                    // ✅ PRIORITÉ EMAIL NON VÉRIFIÉ
                    if (exception instanceof DisabledException
                            || (exception.getMessage() != null
                            && exception.getMessage().contains("EMAIL_NOT_VERIFIED"))) {

                        response.sendRedirect("/auth/login?error=email_not_verified");
                        return;
                    }

                    // ❌ Autres cas (mauvais mot de passe / utilisateur inexistant)
                    response.sendRedirect("/auth/login?error=bad_credentials");
                })

                .permitAll()
                .and()
                .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/auth/login?logout")
                .permitAll();

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);

        // Permet de ne pas masquer DisabledException (email non vérifié)
        provider.setHideUserNotFoundExceptions(false);

        return provider;
    }

}