package com.freelms.lms.auth.mapper;

import com.freelms.lms.auth.dto.RegisterRequest;
import com.freelms.lms.auth.dto.UserDto;
import com.freelms.lms.auth.entity.User;
import com.freelms.lms.common.enums.UserRole;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "level", constant = "1")
    @Mapping(target = "totalPoints", constant = "0")
    @Mapping(target = "failedLoginAttempts", constant = "0")
    @Mapping(target = "role", source = "role", defaultExpression = "java(com.freelms.lms.common.enums.UserRole.STUDENT)")
    @BeanMapping(builder = @Builder(disableBuilder = true))
    User toEntity(RegisterRequest request);

    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    @Mapping(target = "active", expression = "java(user.isActive())")
    @Mapping(target = "emailVerified", expression = "java(user.isEmailVerified())")
    UserDto toDto(User user);

    List<UserDto> toDtoList(List<User> users);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(UserDto dto, @MappingTarget User user);
}
