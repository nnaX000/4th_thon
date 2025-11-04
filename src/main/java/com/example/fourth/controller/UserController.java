package com.example.fourth.controller;

import com.example.fourth.entity.User;
import com.example.fourth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 유저 생성
    @Operation(
            summary = "회원가입 - 김나영",
            description = "새로운 사용자를 생성합니다. 이메일, 닉네임, 비밀번호를 필수로 입력해야 합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(schema = @Schema(
                            example = """
                {
                  "email": "test@example.com",
                  "nickname": "nayoung",
                  "password": "1234"
                }
                """
                    ))
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "중복된 이메일 또는 닉네임")
    })
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        try {
            User createdUser = userService.createUser(user);
            return ResponseEntity.ok(createdUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // 닉네임 중복 검사
    @Operation(
            summary = "닉네임 중복 검사 - 김나영",
            description = "입력한 닉네임이 이미 존재하는지 확인합니다.",
            parameters = {
                    @Parameter(name = "nickname", description = "검사할 닉네임", example = "nayoung", required = true)
            }
    )
    @GetMapping("/check-nickname")
    public ResponseEntity<Map<String, Boolean>> checkNickname(@RequestParam String nickname) {
        boolean isAvailable = userService.isNicknameAvailable(nickname);
        Map<String, Boolean> response = new HashMap<>();
        response.put("available", isAvailable);
        return ResponseEntity.ok(response);
    }

    // 이메일 중복 검사
    @Operation(
            summary = "이메일 중복 검사 - 김나영",
            description = "입력한 이메일이 이미 존재하는지 확인합니다.",
            parameters = {
                    @Parameter(name = "email", description = "검사할 이메일", example = "test@example.com", required = true)
            }
    )
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmail(@RequestParam String email) {
        boolean isAvailable = userService.isEmailAvailable(email);
        Map<String, Boolean> response = new HashMap<>();
        response.put("available", isAvailable);
        return ResponseEntity.ok(response);
    }

    //로그인
    @Operation(
            summary = "로그인 - 김나영",
            description = """
이메일과 비밀번호를 사용하여 로그인합니다.  
로그인 성공 시 회원의 고유 ID(`id`)가 반환됩니다.  
이 `id`는 이후 다른 API 요청 시 사용자 식별용으로 포함되어야 합니다.
""",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(schema = @Schema(
                            example = """
            {
              "email": "test@example.com",
              "password": "1234"
            }
            """
                    ))
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공 (id 반환됨)",
                    content = @Content(schema = @Schema(
                            example = """
            {
              "message": "로그인 성공",
              "id": 1
            }
            """
                    ))
            ),
            @ApiResponse(responseCode = "400", description = "이메일 또는 비밀번호 불일치")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");

        try {
            User user = userService.login(email, password);
            return ResponseEntity.ok(Map.of(
                    "message", "로그인 성공",
                    "id", user.getId()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}