package org.example.vibewall.service;

import lombok.RequiredArgsConstructor;
import org.example.vibewall.DAO.UsersRepo;
import org.example.vibewall.encryption.Encryption;
import org.example.vibewall.model.Users;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService{
    private final UsersRepo userRepository;
    private final Encryption encryption;



    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users user = userRepository.findByUsername(encryption.encode(username))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
    }
}
