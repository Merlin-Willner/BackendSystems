package adapters.api.dto;

public record UserResponse(
        Long userId,
        String username,
        String email
) {
}
