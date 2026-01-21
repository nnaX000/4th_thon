package com.example.fourth.feign;

import com.example.fourth.dto.DiscordWebhookRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name="discordClient",
        url="${discord.webhook-url}"
)
public interface DiscordFeignClient {

    @PostMapping
    void sendError(@RequestBody DiscordWebhookRequest request);
}
