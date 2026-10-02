package post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PublicUpdateRequest(
        @NotBlank @Size(max = 100)
        String title,

        @NotBlank
        String content
) {
}
