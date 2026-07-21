package com.windloo.ai.dto;
import jakarta.validation.constraints.NotBlank;

public record ChatRequest(String conversationId, String episodeId, @NotBlank String message) {}