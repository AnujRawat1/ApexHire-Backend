package com.ApexHire.auth.dto;

import com.ApexHire.user.model.Role;
import com.ApexHire.user.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class AdminUserResponse {

    private String id;
    private String name;
    private String email;
    private Set<Role> roles;

    public static AdminUserResponse from(User user) {

        return new AdminUserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRoles()
        );
    }
}