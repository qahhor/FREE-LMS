package com.freelms.auth.service;

import com.freelms.auth.dto.UpdateUserRequest;
import com.freelms.auth.entity.User;
import com.freelms.auth.mapper.UserMapper;
import com.freelms.auth.repository.UserRepository;
import com.freelms.common.dto.PagedResponse;
import com.freelms.common.dto.UserDto;
import com.freelms.common.enums.UserRole;
import com.freelms.common.exception.ForbiddenException;
import com.freelms.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Cacheable(value = "users", key = "#id")
    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return userMapper.toDto(user);
    }

    @Transactional(readOnly = true)
    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return userMapper.toDto(user);
    }

    @Transactional(readOnly = true)
    public PagedResponse<UserDto> getAllUsers(Pageable pageable) {
        Page<User> usersPage = userRepository.findAll(pageable);
        List<UserDto> userDtos = userMapper.toDtoList(usersPage.getContent());
        return PagedResponse.of(usersPage, userDtos);
    }

    @Transactional(readOnly = true)
    public PagedResponse<UserDto> getUsersByRole(UserRole role, Pageable pageable) {
        Page<User> usersPage = userRepository.findByRole(role, pageable);
        List<UserDto> userDtos = userMapper.toDtoList(usersPage.getContent());
        return PagedResponse.of(usersPage, userDtos);
    }

    @Transactional(readOnly = true)
    public PagedResponse<UserDto> searchUsers(String search, Pageable pageable) {
        Page<User> usersPage = userRepository.searchUsers(search, pageable);
        List<UserDto> userDtos = userMapper.toDtoList(usersPage.getContent());
        return PagedResponse.of(usersPage, userDtos);
    }

    @CacheEvict(value = "users", key = "#id")
    @Transactional
    public UserDto updateUser(Long id, UpdateUserRequest request, Long currentUserId, UserRole currentUserRole) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // Check permissions: user can update own profile, admin can update anyone
        if (!id.equals(currentUserId) && currentUserRole != UserRole.ADMIN) {
            throw new ForbiddenException("You can only update your own profile");
        }

        userMapper.updateEntity(request, user);
        user = userRepository.save(user);

        log.info("User updated: {}", id);
        return userMapper.toDto(user);
    }

    @CacheEvict(value = "users", key = "#id")
    @Transactional
    public void deleteUser(Long id, Long currentUserId, UserRole currentUserRole) {
        if (currentUserRole != UserRole.ADMIN) {
            throw new ForbiddenException("Only administrators can delete users");
        }

        if (id.equals(currentUserId)) {
            throw new ForbiddenException("You cannot delete your own account");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // Soft delete
        user.setActive(false);
        userRepository.save(user);

        log.info("User deactivated: {}", id);
    }

    @CacheEvict(value = "users", key = "#id")
    @Transactional
    public UserDto updateUserRole(Long id, UserRole newRole, Long currentUserId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (id.equals(currentUserId)) {
            throw new ForbiddenException("You cannot change your own role");
        }

        user.setRole(newRole);
        user = userRepository.save(user);

        log.info("User role updated: {} -> {}", id, newRole);
        return userMapper.toDto(user);
    }

    @Transactional
    public void addPoints(Long userId, int points) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setTotalPoints(user.getTotalPoints() + points);

        // Level up logic: every 1000 points = 1 level
        int newLevel = (user.getTotalPoints() / 1000) + 1;
        if (newLevel > user.getLevel()) {
            user.setLevel(newLevel);
            log.info("User {} leveled up to level {}", userId, newLevel);
        }

        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<UserDto> getTopInstructors(int limit) {
        List<User> instructors = userRepository.findTopInstructors(Pageable.ofSize(limit));
        return userMapper.toDtoList(instructors);
    }
}
