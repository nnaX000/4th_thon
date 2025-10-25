package com.example.fourth.dto;

import com.example.fourth.entity.Entrance;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EntranceRequest {
    private int userId;
    private Long extractId;
    private Entrance.OptionType option; // 전체통합 or 특정주제
    private String topic;  // 선택 주제

}
