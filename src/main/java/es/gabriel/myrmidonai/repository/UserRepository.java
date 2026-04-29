package es.gabriel.myrmidonai.repository;

import es.gabriel.myrmidonai.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findUserByUsername(String username);
}
