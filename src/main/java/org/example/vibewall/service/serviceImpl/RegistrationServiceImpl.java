package org.example.vibewall.service.serviceImpl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

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

    @Override
    public TokenResponse loginWithGoogle(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            throw new InvalidCredentialsException("Token cannot be blank");
        }

        try {
            String[] parts = idToken.split("\\.");
            if (parts.length < 2) {
                throw new InvalidCredentialsException("Invalid token format");
            }

            byte[] decodedBytes = Base64.getUrlDecoder().decode(parts[1]);
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> claims = objectMapper.readValue(decodedBytes, new TypeReference<Map<String, Object>>() {});

            Number exp = (Number) claims.get("exp");
            if (exp != null && exp.longValue() * 1000 < System.currentTimeMillis()) {
                throw new InvalidCredentialsException("Token has expired");
            }

            String email = (String) claims.get("email");
            String sub = (String) claims.get("sub");
            if (sub == null) {
                sub = (String) claims.get("user_id");
            }

            String identifier = (email != null && !email.isBlank()) ? email : ("google_" + sub);
            if (identifier == null || identifier.isBlank()) {
                throw new InvalidCredentialsException("Cannot identify user from Google token");
            }

            String name = (String) claims.get("name");
            if (name == null || name.isBlank()) {
                name = identifier;
            }

            String encodedUsername = encryption.encode(identifier);

            Users user = repo.findByUsername(encodedUsername).orElseGet(() -> {
                Users newUser = new Users();
                newUser.setUsername(encodedUsername);
                newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString() + "!A1a"));
                newUser.setRole("USER");
                return repo.save(newUser);
            });

            String accessToken = jwtUtil.generateToken(
                    user.getId(),
                    user.getUsername(),
                    user.getRole(),
                    name
            );

            RefreshToken refreshToken =
                    refreshTokenServiceImplements.createRefreshToken(user.getId());

            return TokenResponse.builder()
                    .jwt(accessToken)
                    .refresh(refreshToken.getToken())
                    .role(user.getRole())
                    .build();

        } catch (InvalidCredentialsException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error authenticating Google user: {}", e.getMessage(), e);
            throw new InvalidCredentialsException("Failed to verify Google token: " + e.getMessage());
        }
    }
}
