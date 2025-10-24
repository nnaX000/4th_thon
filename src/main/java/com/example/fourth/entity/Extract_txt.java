package com.example.fourth.entity;

import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "extract_txt")
public class Extract_txt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    // User 테이블과 외래키 관계 (ManyToOne)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Lob  // LONGTEXT 매핑
    @Column(columnDefinition = "LONGTEXT")
    private String extract;
}
