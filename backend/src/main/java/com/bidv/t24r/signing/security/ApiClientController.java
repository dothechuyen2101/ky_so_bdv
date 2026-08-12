package com.bidv.t24r.signing.security;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/api-clients")
public class ApiClientController {
    private final ApiClientService service;

    public ApiClientController(ApiClientService service) {
        this.service = service;
    }

    @GetMapping
    public List<ApiClientService.ApiClientResponse> list() {
        return service.list();
    }

    @PostMapping
    public ApiClientService.CreateApiClientResponse create(
            @Valid @RequestBody ApiClientService.CreateApiClientRequest request
    ) {
        return service.create(request);
    }
}
