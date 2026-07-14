package com.windloo.model.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
@Data @AllArgsConstructor
public class IdentityStats {
    private long total;
    private long adminCount;
    private long userCount;
}