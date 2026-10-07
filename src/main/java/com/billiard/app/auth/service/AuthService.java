package com.billiard.app.auth.service;

import com.billiard.app.auth.dto.AuthResponse;
import com.billiard.app.auth.dto.LoginRequest;
import com.billiard.app.auth.dto.RegisterRequest;
import com.billiard.app.auth.security.JwtTokenProvider;
import com.billiard.app.common.exception.ErrorCode;
import com.billiard.app.common.exception.ValidationException;
import com.billiard.app.shop.entity.Shop;
import com.billiard.app.shop.entity.User;
import com.billiard.app.shop.repository.ShopRepository;
import com.billiard.app.shop.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public AuthService(ShopRepository shopRepository,
                        UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        JwtTokenProvider jwtTokenProvider,
                        AuthenticationManager authenticationManager) {
        this.shopRepository = shopRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (!request.password().equals(request.confirmPassword())) {
            throw new ValidationException(ErrorCode.PASSWORD_CONFIRMATION_MISMATCH,
                    "Password and confirmation do not match");
        }
        if (userRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new ValidationException(ErrorCode.PHONE_ALREADY_REGISTERED,
                    "Phone number is already registered");
        }

        Shop shop = Shop.builder()
                .name(request.shopName())
                .build();
        shop = shopRepository.save(shop);

        User user = User.builder()
                .shopId(shop.getId())
                .phoneNumber(request.phoneNumber())
                .passwordHash(passwordEncoder.encode(request.password()))
                .build();
        user = userRepository.save(user);

        log.info("Registered new shop and user: shopId={}, userId={}", shop.getId(), user.getId());

        String token = jwtTokenProvider.generateToken(user.getId(), shop.getId(), user.getPhoneNumber());
        return new AuthResponse(token, user.getId(), shop.getId(), shop.getName(), user.getPhoneNumber());
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.phoneNumber(), request.password()));
        } catch (BadCredentialsException ex) {
            throw new BadCredentialsException("Invalid phone number or password");
        }

        User user = userRepository.findByPhoneNumber(request.phoneNumber())
                .orElseThrow(() -> new BadCredentialsException("Invalid phone number or password"));
        Shop shop = shopRepository.findById(user.getShopId())
                .orElseThrow(() -> new IllegalStateException("Shop not found for user: " + user.getId()));

        String token = jwtTokenProvider.generateToken(user.getId(), shop.getId(), user.getPhoneNumber());
        log.info("User logged in: userId={}, shopId={}", user.getId(), shop.getId());
        return new AuthResponse(token, user.getId(), shop.getId(), shop.getName(), user.getPhoneNumber());
    }
}
