package com.example.fourth.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "entrance")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Entrance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User: 다대일 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ExtractTxt: 다대일 관계 (여러 Entrance가 하나의 ExtractTxt와 연결 가능)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "extract_id", nullable = false)
    private Extract_txt extract;

    // enum 타입
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OptionType analysis_option;

    // 주제
    @Column(length = 255)
    private String topic;

    // enum 정의
    public enum OptionType {
        전체통합,
        특정주제
    }
}