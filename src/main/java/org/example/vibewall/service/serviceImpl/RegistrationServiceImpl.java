package org.example.vibewall.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.example.vibewall.DTO.TokenResponse;
import org.example.vibewall.DTO.UserRequest;
import org.example.vibewall.DTO.UserResponse;
import org.example.vibewall.encryption.Encryption;
import org.example.vibewall.exception.InvalidCredentialsException;
import org.example.vibewall.model.RefreshToken;
import org.example.vibewall.model.Users;
import org.example.vibewall.repo.UserRepository;
import org.example.vibewall.security.JwtUtil;
import org.example.vibewall.service.RefreshTokenService;
import org.example.vibewall.service.RegistrationService;
import org.example.vibewall.utility.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.example.vibewall.security.CustomUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository repo;
    private static final Logger logger =
            LoggerFactory.getLogger(RegistrationServiceImpl.class);
    private final Encryption encryption;
    private final UserMapper mapper;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenServiceImplements;


    @Override
    public UserResponse adduser(UserRequest request) {

        if (repo.existsByUsername((encryption.encode(request.getName())))) {
            throw new RuntimeException("User already exists");
        }

        String role = "USER";
        if (request.getRole() != null) {
            String normalized = request.getRole().toUpperCase();
            if (!normalized.equals("ADMIN") && !normalized.equals("USER")) {
                throw new RuntimeException("Invalid role: must be ADMIN or USER");
            }
            role = normalized;
        }

        Users user = new Users();
        user.setUsername(encryption.encode(request.getName()));
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);

        Users savedUser = repo.save(user);

        return UserResponse.builder().name(request.getName()).id(savedUser.getId()).build();
    }


    @Override
    public TokenResponse login(UserRequest request) {

        String encodedUsername = encryption.encode(request.getName());

        Users user = repo.findByUsername(encodedUsername)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        String accessToken = jwtUtil.generateToken(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                request.getName()
        );

        RefreshToken refreshToken =
                refreshTokenServiceImplements.createRefreshToken(user.getId());

        return TokenResponse.builder()
                .jwt(accessToken)
                .refresh(refreshToken.getToken())
                .role(user.getRole())
                .build();
    }

    @Override
    public TokenResponse refresh(String refreshToken) {

        RefreshToken token =
                refreshTokenServiceImplements.findByToken(refreshToken);

        Users user = repo.findById(token.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String newAccessToken = jwtUtil.generateToken(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                encryption.decode(user.getUsername())
        );

        return TokenResponse.builder()
                .jwt(newAccessToken)
                .refresh(refreshToken)
                .build();
    }

    @Override
    public void logout() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        refreshTokenServiceImplements.deleteByUserId(userDetails.getId());
        SecurityContextHolder.clearContext();
    }
}
