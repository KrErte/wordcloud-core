package ee.bitweb.wordcloud_core.controller;

import ee.bitweb.wordcloud_core.dto.JobResultResponse;
import ee.bitweb.wordcloud_core.dto.SubmitResponse;
import ee.bitweb.wordcloud_core.service.TextService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/texts")
@RequiredArgsConstructor
public class TextController {

    private final TextService textService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SubmitResponse> upload(@RequestParam("file") MultipartFile file) {
        UUID jobId = textService.submit(file);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new SubmitResponse(jobId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobResultResponse> getResult(@PathVariable UUID id) {
        return ResponseEntity.ok(textService.getResult(id));
    }
}
