package com.ApexHire.auth.controller;

import com.ApexHire.auth.dto.AdminUserResponse;
import com.ApexHire.auth.service.AdminUserService;
import com.ApexHire.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminUserService adminUserService;

    @GetMapping("/test")
    public String adminTest() {
        return "Welcome Admin!";
    }

    @GetMapping
    public List<AdminUserResponse> getAllUsers() {

        return adminUserService
                .getAllUsers()
                .stream()
                .map(AdminUserResponse::from)
                .toList();
    }

    @GetMapping("/{email}")
    public AdminUserResponse getUserByEmail(@PathVariable String email) {

        User user = adminUserService.getUserByEmail(email);

        return AdminUserResponse.from(user);
    }
}