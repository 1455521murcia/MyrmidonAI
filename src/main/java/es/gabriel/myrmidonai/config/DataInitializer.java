package es.gabriel.myrmidonai.config;

import es.gabriel.myrmidonai.model.Role;
import es.gabriel.myrmidonai.model.User;
import es.gabriel.myrmidonai.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
@Configuration
public class DataInitializer {


    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder){

        return args -> {
            if (userRepository.findUserByUsername("admin").isEmpty()){
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("deamu"));
                admin.setRole(Role.ROLE_PARTNER);
                userRepository.save(admin);
                System.out.println("Myrmidon: Usuario inicial 'admin' creado.");
            }
            if (userRepository.findUserByUsername("becario").isEmpty()){
                User becario = new User();
                becario.setUsername("becario");
                becario.setPassword(passwordEncoder.encode("becario123"));
                becario.setRole(Role.ROLE_INTERN);
                userRepository.save(becario);
                System.out.println("Myrmidon: Usuario 'becario' creado.");
            }
        };
    }
}
