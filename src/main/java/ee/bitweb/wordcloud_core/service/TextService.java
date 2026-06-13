package ee.bitweb.wordcloud_core.service;

import ee.bitweb.wordcloud_core.dto.JobResultResponse;
import ee.bitweb.wordcloud_core.dto.WordCountDto;
import ee.bitweb.wordcloud_core.entity.JobStatus;
import ee.bitweb.wordcloud_core.entity.TextJob;
import ee.bitweb.wordcloud_core.messaging.TextChunkMessage;
import ee.bitweb.wordcloud_core.repository.TextJobRepository;
import ee.bitweb.wordcloud_core.repository.WordCountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TextService {

    private final TextJobRepository textJobRepository;
    private final WordCountRepository wordCountRepository;
    private final RabbitTemplate rabbitTemplate;

    @Value("${wordcloud.rabbitmq.exchange}")
    private String exchange;

    @Value("${wordcloud.rabbitmq.routing-key}")
    private String routingKey;

    @Transactional
    public UUID submit(MultipartFile file) {
        String content;
        try {
            content = new String(file.getBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to read file");
        }

        TextJob job = new TextJob();
        job.setId(UUID.randomUUID());
        job.setStatus(JobStatus.PENDING);
        job.setFilename(file.getOriginalFilename());
        textJobRepository.save(job);

        TextChunkMessage message = new TextChunkMessage(job.getId(), file.getOriginalFilename(), content);
        rabbitTemplate.convertAndSend(exchange, routingKey, message);

        log.info("Submitted job {} for file '{}'", job.getId(), file.getOriginalFilename());
        return job.getId();
    }

    @Transactional(readOnly = true)
    public JobResultResponse getResult(UUID id) {
        TextJob job = textJobRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found: " + id));

        List<WordCountDto> words = wordCountRepository.findByJobIdOrderByCountDesc(id)
                .stream()
                .map(wc -> new WordCountDto(wc.getWord(), wc.getCount()))
                .toList();

        return new JobResultResponse(id, job.getStatus().name(), words);
    }
}
