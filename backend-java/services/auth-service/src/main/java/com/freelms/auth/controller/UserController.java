package com.freelms.auth.controller;

import com.freelms.auth.dto.UpdateUserRequest;
import com.freelms.auth.service.UserService;
import com.freelms.common.dto.ApiResponse;
import com.freelms.common.dto.PagedResponse;
import com.freelms.common.dto.UserDto;
import com.freelms.common.enums.UserRole;
import com.freelms.common.security.CurrentUser;
import com.freelms.common.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management endpoints")
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users (Admin only)")
    public ResponseEntity<ApiResponse<PagedResponse<UserDto>>> getAllUsers(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PagedResponse<UserDto> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @Operation(summary = "Search users")
    public ResponseEntity<ApiResponse<PagedResponse<UserDto>>> searchUsers(
            @RequestParam String q,
            @PageableDefault(size = 20) Pageable pageable) {
        PagedResponse<UserDto> users = userService.searchUsers(q, pageable);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get users by role (Admin only)")
    public ResponseEntity<ApiResponse<PagedResponse<UserDto>>> getUsersByRole(
            @PathVariable UserRole role,
            @PageableDefault(size = 20) Pageable pageable) {
        PagedResponse<UserDto> users = userService.getUsersByRole(role, pageable);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/instructors/top")
    @Operation(summary = "Get top instructors")
    public ResponseEntity<ApiResponse<List<UserDto>>> getTopInstructors(
            @RequestParam(defaultValue = "10") int limit) {
        List<UserDto> instructors = userService.getTopInstructors(limit);
        return ResponseEntity.ok(ApiResponse.success(instructors));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable Long id) {
        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<ApiResponse<UserDto>> updateCurrentUser(
            @CurrentUser UserPrincipal userPrincipal,
            @Valid @RequestBody UpdateUserRequest request) {
        UserDto user = userService.updateUser(
                userPrincipal.getId(),
                request,
                userPrincipal.getId(),
                userPrincipal.getRole()
        );
        return ResponseEntity.ok(ApiResponse.success(user, "Profile updated successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user by ID (Admin only)")
    public ResponseEntity<ApiResponse<UserDto>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request,
            @CurrentUser UserPrincipal userPrincipal) {
        UserDto user = userService.updateUser(id, request, userPrincipal.getId(), userPrincipal.getRole());
        return ResponseEntity.ok(ApiResponse.success(user, "User updated successfully"));
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user role (Admin only)")
    public ResponseEntity<ApiResponse<UserDto>> updateUserRole(
            @PathVariable Long id,
            @RequestParam UserRole role,
            @CurrentUser UserPrincipal userPrincipal) {
        UserDto user = userService.updateUserRole(id, role, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(user, "User role updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable Long id,
            @CurrentUser UserPrincipal userPrincipal) {
        userService.deleteUser(id, userPrincipal.getId(), userPrincipal.getRole());
        return ResponseEntity.ok(ApiResponse.success(null, "User deleted successfully"));
    }
}
