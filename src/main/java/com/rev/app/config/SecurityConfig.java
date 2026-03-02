package com.rev.app.config;

import com.rev.app.util.Constants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

        private final UserDetailsService userDetailsService;
        private final PasswordEncoder passwordEncoder;
        private final CustomWebAuthenticationDetailsSource authenticationDetailsSource;
        private final CustomAuthenticationFailureHandler authenticationFailureHandler;
        private final CustomAuthenticationProvider customAuthenticationProvider;

        public SecurityConfig(UserDetailsService userDetailsService,
                        PasswordEncoder passwordEncoder,
                        CustomWebAuthenticationDetailsSource authenticationDetailsSource,
                        CustomAuthenticationFailureHandler authenticationFailureHandler,
                        CustomAuthenticationProvider customAuthenticationProvider) {
                this.userDetailsService = userDetailsService;
                this.passwordEncoder = passwordEncoder;
                this.authenticationDetailsSource = authenticationDetailsSource;
                this.authenticationFailureHandler = authenticationFailureHandler;
                this.customAuthenticationProvider = customAuthenticationProvider;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                // Disabling CSRF for simplicity in this dev environment (allows JS fetch to
                                // work easily)
                                .csrf(csrf -> csrf.disable())

                                // Defining which users can access which parts of the application
                                .authorizeHttpRequests(auth -> auth
                                                // 1. Everyone can see these (Landing, Login, Register, Static Assets)
                                                .requestMatchers(
                                                                "/", "/login", "/register", "/css/**", "/js/**",
                                                                "/images/**", "/uploads/**", "/favicon.ico", "/error",
                                                                Constants.AUTH_PREFIX + "/**")
                                                .permitAll()

                                                // 2. Everyone can browse music, search, and see details
                                                .requestMatchers("/browse", "/search", "/song/**", "/album/**",
                                                                "/artist/**", "/podcasts/**")
                                                .permitAll()

                                                // 3. ONLY Artists can access these (Strict Role Separation)
                                                .requestMatchers(Constants.ANALYTICS_PREFIX + "/**").hasRole("ARTIST")
                                                .requestMatchers("/dashboard", "/dashboard/**").hasRole("ARTIST")
                                                .requestMatchers("/api/songs", "/api/albums", "/api/podcasts/**")
                                                .hasRole("ARTIST")

                                                // 4. Authenticated users (any role) can access their library and player
                                                .requestMatchers("/api/player/**").authenticated()
                                                .requestMatchers("/my/**").authenticated()

                                                // 5. Any other request requires a login
                                                .anyRequest().authenticated())

                                // Configuring the Login process
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .usernameParameter("email")
                                                .passwordParameter("password")
                                                .authenticationDetailsSource(authenticationDetailsSource)
                                                .failureHandler(authenticationFailureHandler)
                                                .defaultSuccessUrl("/", true) // Always redirect to home on success
                                                .permitAll())

                                // Configuring the Logout process
                                .logout(logout -> logout
                                                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                                                .logoutSuccessUrl("/login?logout=true")
                                                .invalidateHttpSession(true)
                                                .deleteCookies("JSESSIONID", "remember-me")
                                                .permitAll())

                                // Enabling Remember Me (Persistent Auth Cookie)
                                .rememberMe(remember -> remember
                                                .key("revplay-secret-key-2024") // Unique key for this app
                                                .tokenValiditySeconds(2592000) // 30 days
                                                .userDetailsService(userDetailsService)
                                                .rememberMeParameter("remember-me"))

                                // Using our custom authentication provider (database-backed)
                                .authenticationProvider(customAuthenticationProvider);

                return http.build();
        }

        @Bean
        public AuthenticationProvider authenticationProvider() {
                DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
                provider.setUserDetailsService(userDetailsService);
                provider.setPasswordEncoder(passwordEncoder);
                return provider;
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
                return config.getAuthenticationManager();
        }
}
