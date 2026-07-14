package com.windloo.model.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
@Data @AllArgsConstructor
public class CategoryStat {
    private String name;
    private long albumCount;
    private long episodeCount;
}