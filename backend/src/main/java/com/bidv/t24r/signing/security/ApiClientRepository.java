package com.bidv.t24r.signing.security;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiClientRepository extends JpaRepository<ApiClient, String> {
    Optional<ApiClient> findByClientId(String clientId);

    Optional<ApiClient> findByApiKeyHashAndActiveTrue(String apiKeyHash);
}
