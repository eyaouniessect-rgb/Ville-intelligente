package com.ville.gestionincidents.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf().disable()
                .authorizeRequests()

                // 🔓 PUBLIC
                .antMatchers("/auth/login", "/auth/register", "/auth/verify",
                        "/incident/nouveau",
                        "/incident/ajouter",
                        "/incident/success",
                        "/static/css/**", "/static/js/**", "/css/**", "/js/**", "/images/**").permitAll()

                // 🔐 ACCÈS SUPERADMIN
                .antMatchers("/superadmin/**").hasRole("SUPERADMIN")

                // 🔐 ACCÈS CITOYEN
                .antMatchers("/citoyen/**").hasRole("CITOYEN")

                // 🔐 AUTRES REQUÊTES
                .anyRequest().authenticated()
                .and()

                .formLogin()
                .loginPage("/auth/login")
                .loginProcessingUrl("/auth/login")

                // 📌 REDIRECTION INTELLIGENTE BASÉE SUR LE RÔLE
                .successHandler(customAuthenticationSuccessHandler())

                // GESTION DES ERREURS
                .failureHandler((request, response, exception) -> {

                    System.out.println("=================================");
                    System.out.println("❌ LOGIN FAILED");
                    System.out.println("Type : " + exception.getClass().getSimpleName());
                    System.out.println("Message : " + exception.getMessage());
                    System.out.println("=================================");

                    if (exception instanceof DisabledException ||
                            (exception.getMessage() != null &&
                                    exception.getMessage().contains("EMAIL_NOT_VERIFIED"))) {

                        response.sendRedirect("/auth/login?error=email_not_verified");
                        return;
                    }

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
        provider.setHideUserNotFoundExceptions(false);
        return provider;
    }

    /**
     * Gestionnaire de redirection après connexion basé sur le rôle
     */
    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request,
                                                HttpServletResponse response,
                                                Authentication authentication) throws IOException, ServletException {
                
                String redirectUrl = "/citoyen/home"; // Par défaut

                // Rediriger selon le rôle
                for (GrantedAuthority authority : authentication.getAuthorities()) {
                    String role = authority.getAuthority();
                    
                    if (role.equals("ROLE_SUPERADMIN")) {
                        redirectUrl = "/superadmin/dashboard";
                        break;
                    } else if (role.equals("ROLE_ADMIN")) {
                        redirectUrl = "/admin/dashboard"; // À créer si nécessaire
                        break;
                    } else if (role.equals("ROLE_AGENT")) {
                        redirectUrl = "/agent/dashboard"; // À créer si nécessaire
                        break;
                    }
                    // ROLE_CITOYEN garde la valeur par défaut
                }

                System.out.println("✅ Redirection après login vers : " + redirectUrl);
                response.sendRedirect(redirectUrl);
            }
        };
    }
}
