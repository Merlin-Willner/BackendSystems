package adapters.api.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserRequest(
        @NotBlank String username,
        @Email @NotBlank String email,
        @NotBlank String password
) {}