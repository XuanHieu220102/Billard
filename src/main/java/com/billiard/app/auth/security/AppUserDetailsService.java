package com.billiard.app.auth.security;

import com.billiard.app.shop.entity.User;
import com.billiard.app.shop.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Loads a user by phone number for Spring Security's authentication manager.
 * The password hash returned here is compared by {@link
 * org.springframework.security.crypto.password.PasswordEncoder}, never
 * exposed further.
 */
@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public AppUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String phoneNumber) throws UsernameNotFoundException {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + phoneNumber));
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getPhoneNumber())
                .password(user.getPasswordHash())
                .authorities("ROLE_USER")
                .build();
    }
}
