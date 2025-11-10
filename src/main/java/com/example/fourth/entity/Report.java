package com.example.fourth.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrance_id")
    private Entrance entrance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id")
    private Folder folder;

    @Column(length = 1000)
    private String title;

    public enum ReportOption {
        TOPIC,   // 주제별 리포트
        TOTAL    // 통합 리포트
    }

    @Enumerated(EnumType.STRING)
    private ReportOption options; // 주제별 or 통합

    private boolean notion; // 노션 업로드 여부 (true/false)

    public enum TagOption {
        POST,   // 게시
        REVIEW    // 검토
    }

    @Enumerated(EnumType.STRING)
    private TagOption tag; // 게시 or 검토

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String content; // JSON 문자열로 저장되는 전체 리포트 본문

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
