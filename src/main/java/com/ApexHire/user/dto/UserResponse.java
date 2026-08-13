package com.ApexHire.user.dto;

import com.ApexHire.user.model.Role;
import com.ApexHire.user.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class UserResponse {

    private String id;
    private String name;
    private String email;
    private Set<Role> roles;

    public static UserResponse from(User user) {

        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRoles()
        );
    }
}