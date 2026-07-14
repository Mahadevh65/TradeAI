package com.trademind.ai.controller;

import com.trademind.ai.dto.AiQueryRequest;
import com.trademind.ai.dto.AiQueryResponse;
import com.trademind.ai.service.AiCopilotService;
import com.trademind.auth.dto.ApiResponse;
import com.trademind.auth.entity.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Tag(name = "AI Copilot", description = "Ask natural-language questions about stocks and your portfolio")
public class AiCopilotController {

    private final AiCopilotService aiCopilotService;

    @PostMapping("/ask")
    public ApiResponse<AiQueryResponse> ask(@AuthenticationPrincipal User user,
                                             @Valid @RequestBody AiQueryRequest request) {
        return ApiResponse.success("OK", aiCopilotService.ask(user, request));
    }
}
