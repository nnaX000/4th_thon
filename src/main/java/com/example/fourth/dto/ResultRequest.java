package com.example.fourth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResultRequest {
    private String userId;
    private Long entranceId;
    private Long topic;
    private String content;
}
