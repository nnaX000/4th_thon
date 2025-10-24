package com.example.fourth.controller;

import com.example.fourth.dto.EntranceRequest;
import com.example.fourth.entity.Entrance;
import com.example.fourth.entity.Result;
import com.example.fourth.repository.EntranceRepository;
import com.example.fourth.repository.ResultRepository;
import com.example.fourth.service.ChatCrawlerService;
import com.example.fourth.service.EntranceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/entrance")
public class EntranceController {

    @Autowired
    private final EntranceService entranceService;
    @Autowired
    private final EntranceRepository entranceRepository;
    @Autowired
    private final ResultRepository resultRepository;

    public EntranceController(EntranceService entranceService, EntranceRepository entranceRepository, ResultRepository resultRepository) {
        this.entranceService = entranceService;
        this.entranceRepository = entranceRepository;
        this.resultRepository = resultRepository;
    }

    //하고 싶은 주제받아서 세션 시작, entrance id 반환
    @PostMapping("/start")
    public ResponseEntity<Long> startAnalysis(@RequestBody EntranceRequest request) {
        Long entranceId = entranceService.createEntrance(request);
        return ResponseEntity.ok(entranceId);
    }

//    @GetMapping(value = "/progress", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public Flux<String> analyze(@RequestParam Long entranceId) {
//        return Flux.create(emitter -> {
//            try {
//                Entrance entrance = entranceRepository.findById(entranceId)
//                        .orElseThrow(() -> new IllegalArgumentException("entrance 없음"));
//
//                String topic = entrance.getTopic();                 // 사용자가 선택한 주제
//                String content = entrance.getExtract().getExtract();// 원문 텍스트
//
//                // 진행률 설계 (단계 기준): 1:20% → 2:50% → 3:85% → 4:100%
//                int progress = 0;
//
//                // ========== 1. 전처리 ==========
//                emitter.next(json("start","전처리", topic, progress, null));
//                // 실제 전처리 로직 (정규화/클리닝/토큰 제한 등)
//                preprocessingService.run(content);
//                progress = 20;
//                emitter.next(json("done","전처리", topic, progress, null));
//
//                // ========== 2. 엔티티(주제) 추출 ==========
//                emitter.next(json("start","엔티티 추출", topic, progress, null));
//                String entities = openAIService.extractEntities(content, topic); // GPT 호출
//                progress = 50;
//                emitter.next(json("done","엔티티 추출", topic, progress,
//                        Map.of("entities", entities)));
//
//                // ========== 3. 새로알게된 개념 + 바로 잡은 개념 분석 ==========
//                emitter.next(json("start","개념 분석", topic, progress, null));
//
//                // 3-1. 새로 알게된 개념
//                String newCcContent = openAIService.analyzeNewConcepts(topic, content); // GPT 호출
//                int newConceptCount = countItems(newCcContent); // 개수 산정(간단 파서/규칙)
//                progress = 65;
//                emitter.next(json("update","개념 분석", topic, progress,
//                        Map.of("new_concept", newConceptCount, "new_cc_content", newCcContent)));
//
//                // 3-2. 바로 잡은 개념
//                String redirectCcContent = openAIService.analyzeRedirectConcepts(topic, content); // GPT 호출
//                int redirectConceptCount = countItems(redirectCcContent);
//                progress = 85;
//                emitter.next(json("done","개념 분석", topic, progress,
//                        Map.of("redirect_concept", redirectConceptCount,
//                                "redirect_cc_content", redirectCcContent)));
//
//                // ========== 4. 자료 큐레이션 ==========
//                emitter.next(json("start","자료 큐레이션", topic, progress, null));
//                String reference = openAIService.curateReferences(topic, content); // GPT 호출
//                progress = 100;
//                emitter.next(json("done","자료 큐레이션", topic, progress,
//                        Map.of("reference", reference)));
//
//                // ✅ 결과 저장 (result 테이블)
//                Result result = Result.builder()
//                        .entrance(entrance)
//                        .user(entrance.getUser())
//                        .topic(topic)
//                        .newConcept(newConceptCount)
//                        .newCcContent(newCcContent)
//                        .redirectConcept(redirectConceptCount)
//                        .redirectCcContent(redirectCcContent)
//                        .reference(reference)
//                        .createdAt(LocalDateTime.now())
//                        .build();
//                resultRepository.save(result);
//
//                // 전체 완료 이벤트
//                emitter.next(json("all_done","전체", topic, progress, null));
//                emitter.complete();
//
//            } catch (Exception e) {
//                emitter.next(json("error","오류", null, 0, Map.of("message", e.getMessage())));
//                emitter.complete();
//            }
//        });
//    }
//
//    /** 진행 이벤트를 JSON 문자열로 생성 */
//    private String json(String phase, String step, String topic, int progress, Map<String, Object> payload) {
//        StringBuilder sb = new StringBuilder();
//        sb.append("{")
//                .append("\"phase\":\"").append(phase).append("\",") // start | update | done | all_done | error
//                .append("\"step\":\"").append(step).append("\",")   // 전처리 | 엔티티 추출 | 개념 분석 | 자료 큐레이션
//                .append("\"topic\":").append(topic==null?"null":"\""+topic+"\"").append(",")
//                .append("\"progress\":").append(progress);
//        if (payload != null && !payload.isEmpty()) {
//            sb.append(",\"payload\":").append(toJsonObject(payload));
//        }
//        sb.append("}");
//        return sb.toString();
//    }
//
//    private String toJsonObject(Map<String, Object> map) {
//        // 간단 직렬화 (gson 쓰면 더 깔끔: new Gson().toJson(map))
//        StringBuilder sb = new StringBuilder("{");
//        boolean first = true;
//        for (Map.Entry<String,Object> e : map.entrySet()) {
//            if (!first) sb.append(",");
//            first = false;
//            sb.append("\"").append(e.getKey()).append("\":");
//            Object v = e.getValue();
//            if (v == null) sb.append("null");
//            else if (v instanceof Number || v instanceof Boolean) sb.append(v.toString());
//            else sb.append("\"").append(v.toString().replace("\"","\\\"")).append("\"");
//        }
//        sb.append("}");
//        return sb.toString();
//    }
//
//    /** 개수 계산 도우미(줄바꿈/리스트 기준 등 규칙으로 간단 집계) */
//    private int countItems(String block) {
//        if (block == null || block.isBlank()) return 0;
//        // 예: 줄바꿈 기준 항목 수 추정
//        return (int) Arrays.stream(block.split("\\r?\\n"))
//                .map(String::trim)
//                .filter(s -> !s.isEmpty())
//                .count();
//    }
}
