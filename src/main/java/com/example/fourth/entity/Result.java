package com.example.fourth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "result")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Result {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrance_id", nullable = false)
    private Entrance entrance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "topic")
    private String topic;

    @Column(name = "new_concept")
    private int newConcept;

    @Column(name = "new_cc_content", columnDefinition = "JSON")
    private String newCcContent;

    @Column(name = "redirect_concept")
    private int redirectConcept;

    @Column(name = "redirect_cc_content", columnDefinition = "JSON")
    private String redirectCcContent;

    @Column(name = "reference", columnDefinition = "JSON")
    private String reference;

    @Column(name = "officials", columnDefinition = "JSON")
    private String officials;

    @Column(name = "extra_user", columnDefinition = "JSON")
    private String extraUser;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
