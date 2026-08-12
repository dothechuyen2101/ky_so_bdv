package com.bidv.t24r.signing.user;

import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserAccountService {
    private final UserAccountRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserAccountService(UserAccountRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void ensureAdmin(String username, String password) {
        if (!repository.existsByUsernameIgnoreCase(username)) {
            create(new CreateUserRequest(username, "Administrator", password, UserRole.ADMIN));
        }
    }

    @Transactional(readOnly = true)
    public List<UserResponse> list() {
        return repository.findAll().stream().map(UserResponse::from).toList();
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        String username = request.username().trim();
        if (repository.existsByUsernameIgnoreCase(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }
        UserAccount account = new UserAccount();
        account.setUsername(username);
        account.setDisplayName(request.displayName().trim());
        account.setPasswordHash(passwordEncoder.encode(request.password()));
        account.setRole(request.role());
        account.setActive(true);
        return UserResponse.from(repository.save(account));
    }

    @Transactional
    public UserResponse update(String id, UpdateUserRequest request) {
        UserAccount account = get(id);
        account.setDisplayName(request.displayName().trim());
        account.setRole(request.role());
        account.setActive(request.active());
        if (request.password() != null && !request.password().isBlank()) {
            account.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        return UserResponse.from(account);
    }

    private UserAccount get(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    public record CreateUserRequest(
            @jakarta.validation.constraints.NotBlank String username,
            @jakarta.validation.constraints.NotBlank String displayName,
            @jakarta.validation.constraints.NotBlank
            @jakarta.validation.constraints.Size(min = 8) String password,
            @jakarta.validation.constraints.NotNull UserRole role
    ) {}

    public record UpdateUserRequest(
            @jakarta.validation.constraints.NotBlank String displayName,
            @jakarta.validation.constraints.NotNull UserRole role,
            boolean active,
            @jakarta.validation.constraints.Size(min = 8) String password
    ) {}

    public record UserResponse(String id, String username, String displayName, UserRole role,
                               boolean active, Instant createdAt, Instant updatedAt) {
        static UserResponse from(UserAccount account) {
            return new UserResponse(account.getId(), account.getUsername(), account.getDisplayName(),
                    account.getRole(), account.isActive(), account.getCreatedAt(), account.getUpdatedAt());
        }
    }
}
