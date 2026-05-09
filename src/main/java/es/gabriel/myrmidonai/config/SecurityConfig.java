package es.gabriel.myrmidonai.config;

import es.gabriel.myrmidonai.model.User;
import es.gabriel.myrmidonai.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Usamos BCryptPasswordEncoder para encriptar y comprobar las contraseñas de los usuarios
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
    // Metodo para buscar usuarios en la BD
    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository){
        return username -> {
            User myUser = userRepository.findUserByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

            // Pasamos de el User de mi modelo a uno que entienda Spring Security
            return new org.springframework.security.core.userdetails.User(
                    myUser.getUsername(),
                    myUser.getPassword(),
                    // Se usa ROLE_ porque Spring lo suele pedir asi
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_" +
                            myUser.getRole().name()))
            );
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                //TODO hay que activarlo cuando esté lo demás montado
                //Desactivado facilita las pruebas
                .csrf(AbstractHttpConfigurer::disable)
                // Autorizacion de rutas
                .authorizeHttpRequests(auth -> auth
                        // Dejamos pasar estos para que puedan ver los estilos
                        .requestMatchers("/login/css/**","/js/**").permitAll()
                        // Para que se puedan logear
                        .requestMatchers("/").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        // TODO cuando termine el front hay que cambiarlo por /login
                        .defaultSuccessUrl("/chat", true) // A dónde va si el login es correcto
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout") // A donde va al salir
                        .invalidateHttpSession(true)       // Destruye la memoria de la sesión
                        .deleteCookies("JSESSIONID")       // Borra la session
                        .permitAll()
                )
                .sessionManagement(session -> session
                        .maximumSessions(1) // Evita que el mismo usuario esté logueado en dos PCs a la vez
                );

        return http.build();
    }
}
