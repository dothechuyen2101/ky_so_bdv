package com.bidv.t24r.signing.user;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserAccountController {
    private final UserAccountService service;

    public UserAccountController(UserAccountService service) { this.service = service; }

    @GetMapping
    public List<UserAccountService.UserResponse> list() { return service.list(); }

    @PostMapping
    public UserAccountService.UserResponse create(
            @Valid @RequestBody UserAccountService.CreateUserRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public UserAccountService.UserResponse update(@PathVariable String id,
            @Valid @RequestBody UserAccountService.UpdateUserRequest request) {
        return service.update(id, request);
    }
}
