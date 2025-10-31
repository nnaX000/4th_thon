package com.example.fourth.service;

import com.example.fourth.entity.User;
import com.example.fourth.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 사용자 생성 서비스 (비밀번호 암호화 추가)
    public User createUser(User user) {
        //닉네임 중복 검사
        if (userRepository.existsByNickname(user.getNickname())) {
            throw new IllegalArgumentException("닉네임이 존재합니다.");
        }

        //이메일 중복 검사
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("이메일이 존재합니다.");
        }

        // 비밀번호 암호화
        String encryptedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encryptedPassword);

        return userRepository.save(user);
    }

    //닉네임 중복 체크
    public boolean isNicknameAvailable(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    //이메일 중복 체크
    public boolean isEmailAvailable(String email) {
        return userRepository.existsByEmail(email);
    }

    //로그인
    public User login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일이 존재하지 않습니다."));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        return user;
    }

    public void saveNotionToken(String email, String token) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 없습니다."));
        user.setNotionToken(token);
        userRepository.save(user);
    }

    public boolean hasNotionToken(String email) {
        return userRepository.findByEmail(email)
                .map(user -> user.getNotionToken()!=null && !user.getNotionToken().isBlank())
                .orElse(false);
    }

}
