package ee.bitweb.wordcloud_core.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "word_count")
@Getter
@Setter
@NoArgsConstructor
public class WordCount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_id", nullable = false)
    private UUID jobId;

    @Column(nullable = false)
    private String word;

    @Column(nullable = false)
    private Integer count;
}
