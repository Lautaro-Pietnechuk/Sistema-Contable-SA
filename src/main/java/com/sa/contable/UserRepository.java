package com.sa.contable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.sa.contable.Usuario;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsernameAndPassword(String username, String password);
    Optional<Usuario> findByUsername(String username);
}
