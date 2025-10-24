package com.example.fourth.service;

import com.example.fourth.dto.EntranceRequest;
import com.example.fourth.entity.Entrance;
import com.example.fourth.entity.Extract_txt;
import com.example.fourth.entity.User;
import com.example.fourth.repository.EntranceRepository;
import com.example.fourth.repository.ExtractRepository;
import com.example.fourth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EntranceService {

    private final UserRepository userRepository;
    private final ExtractRepository extractTxtRepository;
    private final EntranceRepository entranceRepository;

    public Long createEntrance(EntranceRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저"));
        Extract_txt extract = extractTxtRepository.findById(request.getExtractId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 extract"));

        Entrance entrance = Entrance.builder()
                .user(user)
                .extract(extract)
                .analysis_option(Entrance.OptionType.valueOf(request.getOption()))
                .topic(request.getTopic())
                .build();

        entranceRepository.save(entrance);
        return entrance.getId();
    }
}