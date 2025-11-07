package com.example.fourth.controller;

import com.example.fourth.dto.MyPageResponse;
import com.example.fourth.service.MyPageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/mypage")
@RequiredArgsConstructor
public class MyPageController {
    private final MyPageService myPageService;

    @Operation(
            summary = "마이페이지 보기 - 김도윤",
            description = """
                사용자의 닉네임을 반환합니다.  
                또한 새로운 개념 수, 증감, 바로 잡은 개념 수, 증감, 게시 리포트 수, 증감, 총 세션을 반환합니다.
                오늘의 리포트의 id와 title을 반환합니다.
                """
    )
    @GetMapping
    public ResponseEntity<MyPageResponse> getMyPage(@RequestParam int userId) {
        MyPageResponse response = myPageService.getMyPage(userId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "사용자 리포트 잔디밭 데이터 조회 - 김도윤",
            description = """
        특정 사용자의 리포트 생성 이력을 날짜별로 집계해 반환합니다.  
        잔디밭 생성을 위해 카운팅한 것입니다...
        """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "잔디밭 데이터 조회 성공",
                            content = @Content(mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                                        [
                                          {"date": "2025-10-01", "count": 2},
                                          {"date": "2025-10-05", "count": 3},
                                          {"date": "2025-10-10", "count": 1}
                                        ]
                                        """
                                    )
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "해당 사용자의 리포트가 존재하지 않음")
            }
    )
    @GetMapping("/activity")
    public List<Map<String, Object>> getUserActivity(@RequestParam int userId) {
        return myPageService.getUserActivity(userId);
    }


}
