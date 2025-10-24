package com.example.fourth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EntranceRequest {
    private Long userId;
    private Long extractId;
    private String option; // 전체통합 or 특정주제
    private String topic;  // 선택 주제
}
