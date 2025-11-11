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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Schedulers;

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
            summary = "세션 생성하기 버튼 눌렀을 시에 사용자가 선택한 옵션들을 보냄 (Entrance 생성) - 김나영",
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


    // ====== 학습 분석 진행 (SSE 실시간 전송) ======
    @Operation(
            summary = "학습 분석 진행 (SSE 실시간 전송) - 김나영",
            description = """
    학습 분석을 단계별로 진행하며, 각 단계별 상태를 **SSE(Server-Sent Events)** 형태로 실시간 전송합니다.
    
    ### 응답 형식 (다음과 같은 형태로 실시간 응답이 전송됩니다.)
    ```
    event:start
    data:{"phase":"start","step":"전처리","topic":"SSL TLS","progress":0}

    event:done
    data:{"phase":"done","step":"전처리","topic":"SSL TLS","progress":20}

    event:start
    data:{"phase":"start","step":"엔티티 추출","topic":"SSL TLS","progress":20}

    event:done
    data:{"phase":"done","step":"엔티티 추출","topic":"SSL TLS","progress":40}

    event:update
    data:{"phase":"update","step":"개념 분석","topic":"SSL TLS","progress":65,"payload":{"new_concept_count":3}}
    ```

    ### 주요 단계
    - 전처리 (0 → 20%)
    - 엔티티 추출 (20 → 40%)
    - 개념 분석 (40 → 85%)
    - 자료 큐레이션 (85 → 100%)
    - 완료 시 `event: complete` 전송
    """,
            parameters = {
                    @Parameter(
                            name = "entranceId",
                            description = "분석을 진행할 Entrance 엔티티의 ID",
                            required = true,
                            example = "1"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "SSE 스트림으로 실시간 분석 이벤트 전송",
                            content = @Content(mediaType = MediaType.TEXT_EVENT_STREAM_VALUE)
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 entranceId 요청"
                    )
            }
    )
    @GetMapping(value = "/progress", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> analyze(@RequestParam Long entranceId) {

        // ★ 제네릭을 명시해서 Flux<Object>로 추론되는 걸 막는다
        return Flux.<ServerSentEvent<String>>create(emitter -> {
            try {
                Entrance entrance = entranceRepository.findById(entranceId)
                        .orElseThrow(() -> new IllegalArgumentException("entrance 없음"));

                String topicField = entrance.getTopic();
                List<String> topics = Arrays.stream(topicField.split(","))
                        .map(String::trim)
                        .filter(t -> !t.isEmpty())
                        .toList();

                String content = entrance.getExtract().getExtract();

                // 블로킹 작업(OpenAI 호출 등)을 별도 스레드에서 수행
                Schedulers.boundedElastic().schedule(() -> {
                    try {
                        for (String topic : topics) {
                            int progress = 0;

                            // ===== 1. 전처리 =====
                            emitter.next(event("start", json("start", "전처리", topic, progress, null)));
                            sleep(2000);
                            progress = 20;
                            emitter.next(event("done", json("done", "전처리", topic, progress, null)));

                            // ===== 2. 엔티티 추출 =====
                            emitter.next(event("start", json("start", "엔티티 추출", topic, progress, null)));
                            sleep(2000);
                            progress = 40;
                            emitter.next(event("done", json("done", "엔티티 추출", topic, progress, null)));

                            // ===== 3. 개념 분석 =====
                            emitter.next(event("start", json("start", "개념 분석", topic, progress, null)));

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

                            int newConceptCount = countItems(newConceptContent, "새로알게된");

                            emitter.next(event("update", json("update", "개념 분석", topic, 65,
                                    Map.of("new_concept_count", newConceptCount))));

                            String redirectPrompt = String.format("""
                            사용자가 주제 "%s"를 학습하면서 잘못 알고 있던 개념과,
                            이를 통해 올바르게 바로잡은 이해를 **한 쌍으로** JSON 형식으로 정리해줘.
                            ⚠️ 반드시 아래 구조를 "정확히" 따라야 해.
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
                            """, topic, topic, content);

                            String redirectResponse = openAIService.getTopicFromOpenAI(redirectPrompt);
                            String redirectContent = cleanJsonBlock(extractContent(redirectResponse));
                            String prettyRedirectConcept = prettyJson(redirectContent);

                            int redirectConceptCount = countItems(redirectContent, "바로잡은");

                            emitter.next(event("done", json("done", "개념 분석", topic, 85,
                                    Map.of("new_concept_count", newConceptCount,
                                            "redirect_concept_count", redirectConceptCount))));

                            // ===== 4. 자료 큐레이션 =====
                            emitter.next(event("start", json("start", "자료 큐레이션", topic, 85, null)));

                            String refPrompt = String.format("""
                            주제 "%s"를 더 깊이 이해하기 위한 참고 자료(논문, 튜토리얼, 블로그, 공식문서 등)를
                            **JSON 형식으로만** 2~3개 추천해줘.
                            ⚠️ 반드시 아래 구조를 "정확히" 따라야 해.
                            {
                              "추천자료": {
                                "1": { "제목": "MDN async/await 문서",
                                       "링크": "https://developer.mozilla.org/ko/docs/Learn/JavaScript/Asynchronous/Promises" }
                              }
                            }
                            """, topic, topic);

                            String refResponse = openAIService.getTopicFromOpenAI(refPrompt);
                            String refContent = cleanJsonBlock(extractContent(refResponse));
                            String prettyRef = prettyJson(refContent);

                            // ===== 5. 공식 문서 =====
                            String officialPrompt = String.format("""
                            주제 "%s"와 가장 관련성 높은 **공식 문서(Official Documentation)** 1개만 JSON 형식으로 추천해줘.
                            ⚠️ 반드시 아래 구조를 "정확히" 따라야 해.
                            {
                              "공식문서": {
                                "제목": "OpenAI API Reference",
                                "링크": "https://platform.openai.com/docs/api-reference"
                              }
                            }
                            """, topic);

                            String officialResponse = openAIService.getTopicFromOpenAI(officialPrompt);
                            String officialContent = cleanJsonBlock(extractContent(officialResponse));
                            String prettyOfficial = prettyJson(officialContent);

                            emitter.next(event("done", json("done", "자료 큐레이션", topic, 100, null)));

                            // ===== DB 저장 =====
                            Result result = Result.builder()
                                    .entrance(entrance)
                                    .user(entrance.getUser())
                                    .topic(topic)
                                    .newConcept(newConceptCount)
                                    .newCcContent(safeJson(prettyNewConcept))
                                    .redirectConcept(redirectConceptCount)
                                    .redirectCcContent(safeJson(prettyRedirectConcept))
                                    .reference(safeJson(prettyRef))
                                    .officials(safeJson(prettyOfficial))
                                    .createdAt(LocalDateTime.now())
                                    .build();

                            resultRepository.save(result);
                        }

                        emitter.next(event("complete", json("all_done", "전체", "모든 주제 완료", 100, null)));
                        emitter.complete();

                    } catch (Exception ex) {
                        emitter.next(event("error", json("error", "오류", null, 0, Map.of("message", ex.getMessage()))));
                        emitter.complete();
                    }
                });

            } catch (Exception e) {
                emitter.next(event("error", json("error", "오류", null, 0, Map.of("message", e.getMessage()))));
                emitter.complete();
            }
        }, FluxSink.OverflowStrategy.BUFFER);
    }

    /* ---------- 헬퍼들 ---------- */

    private static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    private ServerSentEvent<String> event(String name, String data) {
        return ServerSentEvent.<String>builder()
                .event(name)
                .data(data)
                .build();
    }

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

    private int countItems(String json, String key) {
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            if (!obj.has(key)) return 0;
            JsonElement el = obj.get(key);
            if (el.isJsonPrimitive() && "없음".equals(el.getAsString())) return 0;
            if (el.isJsonObject()) return el.getAsJsonObject().entrySet().size();
            return 0;
        } catch (Exception e) { return 0; }
    }

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
        return response; // 실패 시 원문 반환
    }

    private String cleanJsonBlock(String text) {
        if (text == null) return "";
        text = text.replaceAll("(?s)```json\\s*", "");
        text = text.replaceAll("(?s)```", "");
        return text.trim();
    }

    private String prettyJson(String rawJson) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Object json = mapper.readValue(rawJson, Object.class);
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        } catch (Exception e) {
            System.err.println("JSON 파싱 실패: " + e.getMessage());
            return rawJson;
        }
    }

    private String safeJson(String raw) {
        if (raw == null) return "{}";
        try { JsonParser.parseString(raw); return raw; }
        catch (Exception e) {
            String escaped = raw.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n").replace("\r","\\r");
            return String.format("{\"raw_text\": \"%s\"}", escaped);
        }
    }
}