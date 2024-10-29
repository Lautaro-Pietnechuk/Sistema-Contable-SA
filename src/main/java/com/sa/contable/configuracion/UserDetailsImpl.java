package com.sa.contable.configuracion;
/* package com.sa.contable.configuracion;

import com.sa.contable.entidades.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class UserDetailsImpl implements UserDetails {
    private final Long id;
    private final String nombreUsuario;
    private final String contraseña; // Usando el nombre "contraseña"
    private final Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(Long id, String nombreUsuario, String contraseña, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.nombreUsuario = nombreUsuario;
        this.contraseña = contraseña;
        this.authorities = authorities;
    }

    public static UserDetailsImpl build(Usuario usuario) {
        // Suponiendo que Rol implementa GrantedAuthority
        return new UserDetailsImpl(
                usuario.getId(),
                usuario.getNombreUsuario(),
                usuario.getContraseña(),
                Collections.singletonList(usuario.getRol()) // Asegúrate de que Rol implemente GrantedAuthority
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return contraseña; // Retorna la contraseña
    }

    @Override
    public String getUsername() {
        return nombreUsuario; // Retorna el nombre de usuario
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Puedes personalizar la lógica según tu necesidad
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Puedes personalizar la lógica según tu necesidad
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Puedes personalizar la lógica según tu necesidad
    }

    @Override
    public boolean isEnabled() {
        return true; // Puedes personalizar la lógica según tu necesidad
    }

    // Getters adicionales si es necesario
    public Long getId() {
        return id;
    }
}
 */