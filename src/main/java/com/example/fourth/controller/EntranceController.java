package com.example.fourth.controller;

import com.example.fourth.dto.EntranceRequest;
import com.example.fourth.entity.Entrance;
import com.example.fourth.entity.Result;
import com.example.fourth.repository.EntranceRepository;
import com.example.fourth.repository.ResultRepository;
import com.example.fourth.service.EntranceService;
import com.example.fourth.service.OpenAIService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/entrance")
public class EntranceController {

    private final EntranceService entranceService;
    private final EntranceRepository entranceRepository;
    private final ResultRepository resultRepository;
    private final OpenAIService openAIService;

    public EntranceController(EntranceService entranceService,
                              EntranceRepository entranceRepository,
                              ResultRepository resultRepository,
                              OpenAIService openAIService) {
        this.entranceService = entranceService;
        this.entranceRepository = entranceRepository;
        this.resultRepository = resultRepository;
        this.openAIService = openAIService;
    }

    @Operation(
            summary = "세션 생성하기 버튼 눌렀을 시에 사용자가 선택한 옵션들을 보냄 (Entrance 생성)",
            description = """
    사용자의 분석 세션을 시작합니다.  
    - `option`은 무조건 '전체통합' 또는 '특정주제' 중 하나를 선택합니다.  
    - `topic`은 선택한 주제를 **쉼표( , )로 구분**하여 여러 개 보낼 수 있습니다.  
      예: `"SSL,TLS,보안 프로토콜"`
    - 반환되는 entrance-id는 이후 요청에 필요함.
    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "세션 생성 요청 데이터 (사용자 ID, 추출 ID, 옵션, 주제 목록 등)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
            {
              "userId": 1,
              "extractId": 1,
              "option": "특정주제",
              "topic": "SSL,TLS,보안 프로토콜"
            }
            """)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "세션 생성 성공 — entrance-id 반환",
                            content = @Content(mediaType = "application/json",
                                    examples = @ExampleObject(value = """
                42
                """))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청 — 필수 데이터 누락 또는 형식 오류",
                            content = @Content(mediaType = "application/json",
                                    examples = @ExampleObject(value = """
                {
                  "error": "option 또는 topic 값이 유효하지 않습니다."
                }
                """))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "서버 내부 오류 — 세션 생성 중 예외 발생",
                            content = @Content(mediaType = "application/json",
                                    examples = @ExampleObject(value = """
                {
                  "error": "세션 생성 중 오류가 발생했습니다."
                }
                """))
                    )
            }
    )
    @PostMapping("/start")
    public ResponseEntity<Long> startAnalysis(@RequestBody EntranceRequest request) {
        Long entranceId = entranceService.createEntrance(request);
        return ResponseEntity.ok(entranceId); //entrance-id 반환
    }


    //학습분석 진행 및 실시간 전송
    @GetMapping(value = "/progress", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> analyze(@RequestParam Long entranceId) {
        return Flux.create(emitter -> {
            try {
                Entrance entrance = entranceRepository.findById(entranceId)
                        .orElseThrow(() -> new IllegalArgumentException("entrance 없음"));

                // 주제 파싱
                String topicField = entrance.getTopic();

                List<String> topics = Arrays.stream(topicField.split(","))
                        .map(String::trim)
                        .filter(t -> !t.isEmpty())
                        .toList();

                String content = entrance.getExtract().getExtract();

                // ===== 각 주제별로 개별 처리 =====
                for (String topic : topics) {
                    int progress = 0;

                    try {
                        // ===== 1. 전처리 =====
                        emitter.next(json("start", "전처리", topic, progress, null));
                        Mono.delay(Duration.ofSeconds(5)).block();
                        progress = 20;
                        emitter.next(json("done", "전처리", topic, progress, null));

                        // ===== 2. 엔티티 추출 =====
                        emitter.next(json("start", "엔티티 추출", topic, progress, null));
                        Mono.delay(Duration.ofSeconds(5)).block();
                        progress = 40;
                        emitter.next(json("done", "엔티티 추출", topic, progress, null));

                        // ===== 3. 개념 분석 =====
                        emitter.next(json("start", "개념 분석", topic, progress, null));

                        // 3-1 새로 알게된 개념
                        String newConceptPrompt = String.format("""
                            사용자가 학습한 주제 "%s"에 대해 새롭게 알게 된 개념만을 JSON 형식으로 정리해줘.
                            ⚠️ 반드시 아래 형식을 "정확히" 따라야 해. 설명이나 문장, 마크다운, ```json 같은 건 절대 추가하지 마.
                            출력은 무조건 다음 구조로만 반환해:
                            
                            {
                              "새로알게된": {
                                "1": "첫 번째 개념 내용",
                                "2": "두 번째 개념 내용",
                                "3": "세 번째 개념 내용"
                              }
                            }
                            
                            반드시 위 JSON 구조만 반환하고, 추가 문장은 절대 쓰지 마.
                            주제: "%s"
                            대화 내용: %s
                            """, topic, topic, content);

                        String newConceptResponse = openAIService.getTopicFromOpenAI(newConceptPrompt);
                        String newConceptContent = cleanJsonBlock(extractContent(newConceptResponse));
                        String prettyNewConcept = prettyJson(newConceptContent);

                        List<String> newConcepts = parseJsonArray(newConceptContent, "새로알게된");

                        progress = 65;
                        emitter.next(json("update", "개념 분석", topic, progress,
                                Map.of("new_concept", newConcepts.size())));

                        // 3-2 바로 잡은 개념
                        String redirectPrompt = String.format("""
                            사용자가 주제 "%s"를 학습하면서 잘못 알고 있던 개념과,
                            이를 통해 올바르게 바로잡은 이해를 **한 쌍으로** JSON 형식으로 정리해줘.
                            
                            ⚠️ 반드시 아래 구조를 "정확히" 따라야 해.
                            설명, 문장, ```json 코드블록, 추가 문구는 절대 포함하지 마.
                            오직 JSON만 반환해야 해.
                            
                            {
                              "바로잡은": {
                                "1": {
                                  "잘못된이해": "HTTP는 상태를 유지한다.",
                                  "올바른이해": "HTTP는 상태를 유지하지 않는(stateless) 프로토콜이다."
                                },
                                "2": {
                                  "잘못된이해": "SSL과 TLS는 완전히 다른 기술이다.",
                                  "올바른이해": "TLS는 SSL의 개선판이며 현재는 TLS가 표준으로 사용된다."
                                }
                              }
                            }
                            
                            이 구조를 반드시 따르고,
                            해당 주제 "%s"와 관련 없는 내용은 절대 포함하지 마.
                            만약 수정할 개념이 없다면 다음과 같이만 출력해:
                            {"바로잡은": "없음"}
                            
                            대화 내용:
                            %s
                            """, topic, topic, content);

                        String redirectResponse = openAIService.getTopicFromOpenAI(redirectPrompt);
                        String redirectContent = cleanJsonBlock(extractContent(redirectResponse));
                        String prettyRedirectConcept = prettyJson(redirectContent);
                        List<String> redirectConcepts = parseJsonArray(redirectContent, "바로잡은");

                        progress = 85;
                        emitter.next(json("done", "개념 분석", topic, progress,
                                Map.of("new_concept_list", newConcepts,
                                        "redirect_concept_list", redirectConcepts)));

                        // ===== 4. 자료 큐레이션 =====
                        emitter.next(json("start", "자료 큐레이션", topic, progress, null));

                        String refPrompt = String.format("""
                            주제 "%s"를 더 깊이 이해하기 위한 참고 자료(논문, 튜토리얼, 블로그, 공식문서 등)를
                            **JSON 형식으로만** 2~3개 추천해줘.
                            
                            ⚠️ 아래 구조를 반드시 지켜야 하며, 
                            그 외의 설명문, 문장, ```json 코드블록, 불필요한 텍스트는 절대 포함하지 마.
                            반드시 JSON 객체만 반환해야 해.
                            
                            {
                              "추천자료": {
                                "1": {
                                  "제목": "MDN async/await 문서",
                                  "링크": "https://developer.mozilla.org/ko/docs/Learn/JavaScript/Asynchronous/Promises"
                                },
                                "2": {
                                  "제목": "모던 자바스크립트 딥다이브 13장 - 비동기 처리",
                                  "링크": "https://poiemaweb.com/js-async"
                                }
                              }
                            }
                            
                            위 구조를 반드시 따르고,
                            주제 "%s"와 직접적으로 관련된 자료만 포함해.
                            관련 자료가 전혀 없으면 다음과 같이 출력해:
                            {"추천자료": "없음"}
                            """, topic, topic);

                        String refResponse = openAIService.getTopicFromOpenAI(refPrompt);
                        String refContent = cleanJsonBlock(extractContent(refResponse));
                        String prettyRef = prettyJson(refContent);
                        List<String> references = parseJsonArray(refContent, "추천자료");

                        progress = 100;
                        emitter.next(json("done", "자료 큐레이션", topic, progress,
                                Map.of("reference", references)));

                        // DB 저장
                        Result result = Result.builder()
                                .entrance(entrance)
                                .user(entrance.getUser())
                                .topic(topic)
                                .newConcept(newConcepts.size())
                                .newCcContent(safeJson(prettyNewConcept))
                                .redirectConcept(redirectConcepts.size())
                                .redirectCcContent(safeJson(prettyRedirectConcept))
                                .reference(safeJson(prettyRef))
                                .createdAt(LocalDateTime.now())
                                .build();

                        resultRepository.save(result);

                    } catch (Exception e) {
                        System.err.println("오류 발생 (topic=" + topic + "): " + e.getMessage());
                        emitter.next(json("error", "오류", topic, progress,
                                Map.of("message", e.getMessage())));
                    }
                }

                emitter.next(json("all_done", "전체", "모든 주제 완료", 100, null));
                emitter.complete();

            } catch (Exception e) {
                emitter.next(json("error", "오류", null, 0, Map.of("message", e.getMessage())));
                emitter.complete();
            }
        });
    }

    /** 진행 이벤트를 JSON 문자열로 생성 */
    private String json(String phase, String step, String topic, int progress, Map<String, Object> payload) {
        StringBuilder sb = new StringBuilder();
        sb.append("{")
                .append("\"phase\":\"").append(phase).append("\",")
                .append("\"step\":\"").append(step).append("\",")
                .append("\"topic\":").append(topic == null ? "null" : "\"" + topic + "\"").append(",")
                .append("\"progress\":").append(progress);

        if (payload != null && !payload.isEmpty()) {
            sb.append(",\"payload\":").append(toJsonObject(payload));
        }
        sb.append("}");
        return sb.toString();
    }

    /** Map을 JSON 객체로 직렬화 */
    private String toJsonObject(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> e : map.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            sb.append("\"").append(e.getKey()).append("\":");
            Object v = e.getValue();
            if (v == null) sb.append("null");
            else if (v instanceof Number || v instanceof Boolean) sb.append(v.toString());
            else sb.append("\"").append(v.toString().replace("\"", "\\\"")).append("\"");
        }
        sb.append("}");
        return sb.toString();
    }

    /** JSON 파싱 유틸 */
    private List<String> parseJsonArray(String json, String key) {
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            JsonArray array = obj.getAsJsonArray(key);
            List<String> list = new ArrayList<>();
            array.forEach(e -> list.add(e.getAsString()));
            return list;
        } catch (Exception e) {
            return Arrays.stream(json.split("\\r?\\n"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
        }
    }

    private String prettyJson(String rawJson) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Object json = mapper.readValue(rawJson, Object.class);
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        } catch (Exception e) {
            System.err.println("JSON 파싱 실패, 원본 그대로 반환: " + e.getMessage());
            return rawJson; // 그대로 반환 (raw_text 감싸지 않음)
        }
    }

    /** GPT 응답 JSON에서 message.content만 추출 */
    private String extractContent(String response) {
        try {
            JsonObject obj = JsonParser.parseString(response).getAsJsonObject();
            JsonArray choices = obj.getAsJsonArray("choices");
            if (choices != null && choices.size() > 0) {
                JsonObject message = choices.get(0).getAsJsonObject().getAsJsonObject("message");
                if (message != null && message.has("content")) {
                    return message.get("content").getAsString().trim();
                }
            }
        } catch (Exception e) {
            System.err.println("GPT 응답 파싱 실패: " + e.getMessage());
        }
        return response;
    }

    // 아래 메서드들을 EntranceController 안쪽에 추가해 주세요
    private String safeJson(String raw) {
        if (raw == null) return "{}";

        try {
            // JSON인지 검사
            com.google.gson.JsonParser.parseString(raw);
            return raw;
        } catch (Exception e) {
            // JSON 아님 → 안전하게 escape해서 문자열로 감싸기
            String escaped = raw
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r");

            return String.format("{\"raw_text\": \"%s\"}", escaped);
        }
    }

    // 코드블록(```json ... ```)을 제거하고 순수 JSON만 남김
    private String cleanJsonBlock(String text) {
        if (text == null) return "";
        text = text.replaceAll("(?s)```json\\s*", "");
        text = text.replaceAll("(?s)```", "");
        return text.trim();
    }


}