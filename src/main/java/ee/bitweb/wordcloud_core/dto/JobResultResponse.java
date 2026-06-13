package ee.bitweb.wordcloud_core.dto;

import java.util.List;
import java.util.UUID;

public record JobResultResponse(
        UUID id,
        String status,
        List<WordCountDto> words
) {
}
