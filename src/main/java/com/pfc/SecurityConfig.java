package com.pfc;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/css/**", "/dist/**", "/plugins/**", "/js/**", "/images/**").permitAll() // Permitir acesso à página de login e arquivos estáticos
                .anyRequest().authenticated() // Exigir autenticação para qualquer outra página
            )
            .formLogin(form -> form
                .loginPage("/login") // Definir a URL da página de login personalizada
                .defaultSuccessUrl("/", true) // Redirecionar para /home após login bem-sucedido
                .permitAll() // Permitir que todos acessem a página de login
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout") // Redirecionar para o login após logout
            )
            .httpBasic(Customizer.withDefaults()); // Permitir autenticação Basic para a API REST
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails administrador = User.withUsername("admin")
                .password("{noop}thindesk")
                .roles("ADMIN")
                .build();
        UserDetails tecnico = User.withUsername("tecnico")
                .password("{noop}thindesk")
                .roles("TECNICO")
                .build();
        UserDetails usuario = User.withUsername("usuario")
                .password("{noop}thindesk")
                .roles("USUARIO")
                .build();
        return new InMemoryUserDetailsManager(administrador, tecnico, usuario);
    }
}
