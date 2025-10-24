package com.example.fourth.dto;

import lombok.Data;

import java.util.List;

@Data
public class ChatGPTResponse {

    private String id;
    private String object;
    private long created;
    private String model;
    private List<Choice> choices;
    private Usage usage;
    private String service_tier;
    private String system_fingerprint;

    @Data
    public static class Choice {
        private int index;
        private Message message;
        private Object logprobs;
        private String finish_reason;
    }

    @Data
    public static class Message {
        private String role;
        private String content;
        private String refusal;  // 해당 부분은 "null" 값도 받을 수 있도록 처리
    }

    @Data
    public static class Usage {
        private int prompt_tokens;
        private int completion_tokens;
        private int total_tokens;
        private PromptTokensDetails prompt_tokens_details;
        private CompletionTokensDetails completion_tokens_details;
    }

    @Data
    public static class PromptTokensDetails {
        private int cached_tokens;
        private int audio_tokens;
    }

    @Data
    public static class CompletionTokensDetails {
        private int reasoning_tokens;
        private int audio_tokens;
        private int accepted_prediction_tokens;
        private int rejected_prediction_tokens;
    }
}
