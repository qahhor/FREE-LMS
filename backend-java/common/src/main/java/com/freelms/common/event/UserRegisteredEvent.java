package com.freelms.common.event;

import com.freelms.common.enums.UserRole;
import lombok.Getter;

@Getter
public class UserRegisteredEvent extends DomainEvent {

    private final Long userId;
    private final String email;
    private final String firstName;
    private final String lastName;
    private final UserRole role;

    public UserRegisteredEvent(Long userId, String email, String firstName, String lastName, UserRole role) {
        super();
        this.userId = userId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
    }
}
