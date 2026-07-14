package com.windloo.model.dto;

/**
 * 带时间戳的字幕句子，用于服务间传输与搜索索引。
 *
 * @param startMs 句子起始时间（毫秒）
 * @param endMs   句子结束时间（毫秒）
 * @param text    句子文本
 */
public record SentenceDTO(long startMs, long endMs, String text) {}
