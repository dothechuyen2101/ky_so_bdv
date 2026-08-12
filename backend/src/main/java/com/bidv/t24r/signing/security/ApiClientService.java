package com.bidv.t24r.signing.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApiClientService {
    private final ApiClientRepository repository;
    private final SecureRandom secureRandom = new SecureRandom();

    public ApiClientService(ApiClientRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ApiClientResponse> list() {
        return repository.findAll().stream().map(ApiClientResponse::from).toList();
    }

    @Transactional
    public CreateApiClientResponse create(CreateApiClientRequest request) {
        String apiKey = generateApiKey();
        ApiClient client = new ApiClient();
        client.setClientId(request.clientId());
        client.setClientName(request.clientName());
        client.setRoles(request.roles() == null || request.roles().isBlank() ? "ROLE_PARTNER" : request.roles());
        client.setActive(true);
        client.setApiKeyHash(hash(apiKey));
        ApiClient saved = repository.save(client);
        return new CreateApiClientResponse(ApiClientResponse.from(saved), apiKey);
    }

    @Transactional(readOnly = true)
    public ApiClient authenticate(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return null;
        }
        return repository.findByApiKeyHashAndActiveTrue(hash(apiKey)).orElse(null);
    }

    @Transactional
    public void ensureDefaultClient(String clientId, String clientName, String apiKey) {
        repository.findByClientId(clientId).orElseGet(() -> {
            ApiClient client = new ApiClient();
            client.setClientId(clientId);
            client.setClientName(clientName);
            client.setRoles("ROLE_PARTNER");
            client.setActive(true);
            client.setApiKeyHash(hash(apiKey));
            return repository.save(client);
        });
    }

    public String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot hash API key", ex);
        }
    }

    private String generateApiKey() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return "t24r_" + HexFormat.of().formatHex(bytes);
    }

    public record CreateApiClientRequest(String clientId, String clientName, String roles) {
    }

    public record CreateApiClientResponse(ApiClientResponse client, String apiKey) {
    }

    public record ApiClientResponse(String id, String clientId, String clientName, String roles, boolean active) {
        static ApiClientResponse from(ApiClient client) {
            return new ApiClientResponse(client.getId(), client.getClientId(), client.getClientName(),
                    client.getRoles(), client.isActive());
        }
    }
}
