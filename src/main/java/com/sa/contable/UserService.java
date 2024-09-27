package com.sa.contable;


import com.sa.contable.Usuario;
import com.sa.contable.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public Optional<Usuario> login(String username, String password) {
        return userRepository.findByUsernameAndPassword(username, password);
    }

    public Usuario register(Usuario user) {
        return userRepository.save(user);
    }

    public boolean isUsernameTaken(String username) {
        return userRepository.findByUsername(username).isPresent();
    }
}

