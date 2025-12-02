package com.freelms.lms.auth.service;

import com.freelms.lms.auth.dto.*;
import com.freelms.lms.auth.entity.RefreshToken;
import com.freelms.lms.auth.entity.User;
import com.freelms.lms.auth.mapper.UserMapper;
import com.freelms.lms.auth.repository.RefreshTokenRepository;
import com.freelms.lms.auth.repository.UserRepository;
import com.freelms.lms.common.enums.UserRole;
import com.freelms.lms.common.exception.BadRequestException;
import com.freelms.lms.common.exception.ConflictException;
import com.freelms.lms.common.exception.ResourceNotFoundException;
import com.freelms.lms.common.exception.UnauthorizedException;
import com.freelms.lms.common.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "jwtExpiration", 604800000L);
        ReflectionTestUtils.setField(authService, "refreshExpiration", 2592000000L);

        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .firstName("Test")
                .lastName("User")
                .role(UserRole.STUDENT)
                .isActive(true)
                .build();

        registerRequest = RegisterRequest.builder()
                .email("test@example.com")
                .password("Password123!")
                .firstName("Test")
                .lastName("User")
                .role(UserRole.STUDENT)
                .build();

        loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("Password123!")
                .build();
    }

    @Test
    @DisplayName("Should register a new user successfully")
    void shouldRegisterNewUser() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userMapper.toEntity(any(RegisterRequest.class))).thenReturn(testUser);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtTokenProvider.generateToken(anyLong(), anyString(), any(UserRole.class))).thenReturn("accessToken");
        when(jwtTokenProvider.generateRefreshToken(anyLong())).thenReturn("refreshToken");
        when(userMapper.toDto(any(User.class))).thenReturn(new UserDto());
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(new RefreshToken());

        // When
        AuthResponse response = authService.register(registerRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("accessToken");
        assertThat(response.getRefreshToken()).isEqualTo("refreshToken");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw ConflictException when email already exists")
    void shouldThrowConflictExceptionWhenEmailExists() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("Should login user successfully")
    void shouldLoginUserSuccessfully() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtTokenProvider.generateToken(anyLong(), anyString(), any(UserRole.class))).thenReturn("accessToken");
        when(jwtTokenProvider.generateRefreshToken(anyLong())).thenReturn("refreshToken");
        when(userMapper.toDto(any(User.class))).thenReturn(new UserDto());
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(new RefreshToken());

        // When
        AuthResponse response = authService.login(loginRequest, "127.0.0.1");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("accessToken");
    }

    @Test
    @DisplayName("Should throw UnauthorizedException for invalid credentials")
    void shouldThrowUnauthorizedExceptionForInvalidCredentials() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When/Then
        assertThatThrownBy(() -> authService.login(loginRequest, "127.0.0.1"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    @DisplayName("Should throw UnauthorizedException for non-existent user")
    void shouldThrowUnauthorizedExceptionForNonExistentUser() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> authService.login(loginRequest, "127.0.0.1"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("Should throw UnauthorizedException for locked account")
    void shouldThrowUnauthorizedExceptionForLockedAccount() {
        // Given
        testUser.setLockedUntil(LocalDateTime.now().plusMinutes(30));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        // When/Then
        assertThatThrownBy(() -> authService.login(loginRequest, "127.0.0.1"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("locked");
    }

    @Test
    @DisplayName("Should logout user successfully")
    void shouldLogoutUserSuccessfully() {
        // When
        authService.logout(1L);

        // Then
        verify(refreshTokenRepository).revokeAllUserTokens(1L);
    }

    @Test
    @DisplayName("Should refresh token successfully")
    void shouldRefreshTokenSuccessfully() {
        // Given
        RefreshToken refreshToken = RefreshToken.builder()
                .token("validRefreshToken")
                .user(testUser)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .deviceInfo("Test Device")
                .ipAddress("127.0.0.1")
                .build();

        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("validRefreshToken")
                .build();

        when(refreshTokenRepository.findByToken("validRefreshToken")).thenReturn(Optional.of(refreshToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);
        when(jwtTokenProvider.generateToken(anyLong(), anyString(), any(UserRole.class))).thenReturn("newAccessToken");
        when(jwtTokenProvider.generateRefreshToken(anyLong())).thenReturn("newRefreshToken");
        when(userMapper.toDto(any(User.class))).thenReturn(new UserDto());

        // When
        AuthResponse response = authService.refreshToken(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("newAccessToken");
        assertThat(response.getRefreshToken()).isEqualTo("newRefreshToken");
        verify(refreshTokenRepository).save(argThat(token -> token.isRevoked()));
    }

    @Test
    @DisplayName("Should throw UnauthorizedException for expired refresh token")
    void shouldThrowUnauthorizedExceptionForExpiredRefreshToken() {
        // Given
        RefreshToken expiredToken = RefreshToken.builder()
                .token("expiredRefreshToken")
                .user(testUser)
                .expiresAt(LocalDateTime.now().minusDays(1))
                .revoked(false)
                .build();

        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("expiredRefreshToken")
                .build();

        when(refreshTokenRepository.findByToken("expiredRefreshToken")).thenReturn(Optional.of(expiredToken));

        // When/Then
        assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("expired or revoked");
    }

    @Test
    @DisplayName("Should throw UnauthorizedException for revoked refresh token")
    void shouldThrowUnauthorizedExceptionForRevokedRefreshToken() {
        // Given
        RefreshToken revokedToken = RefreshToken.builder()
                .token("revokedRefreshToken")
                .user(testUser)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .revoked(true)
                .build();

        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("revokedRefreshToken")
                .build();

        when(refreshTokenRepository.findByToken("revokedRefreshToken")).thenReturn(Optional.of(revokedToken));

        // When/Then
        assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("expired or revoked");
    }

    @Test
    @DisplayName("Should change password successfully")
    void shouldChangePasswordSuccessfully() {
        // Given
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword("Password123!")
                .newPassword("NewPassword123!")
                .confirmPassword("NewPassword123!")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("Password123!", testUser.getPassword())).thenReturn(true);
        when(passwordEncoder.encode("NewPassword123!")).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        authService.changePassword(1L, request);

        // Then
        verify(userRepository).save(argThat(user -> user.getPassword().equals("newEncodedPassword")));
        verify(refreshTokenRepository).revokeAllUserTokens(1L);
    }

    @Test
    @DisplayName("Should throw BadRequestException when current password is incorrect")
    void shouldThrowBadRequestExceptionForWrongOldPassword() {
        // Given
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword("WrongPassword123!")
                .newPassword("NewPassword123!")
                .confirmPassword("NewPassword123!")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("WrongPassword123!", testUser.getPassword())).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> authService.changePassword(1L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Current password is incorrect");
    }
}
