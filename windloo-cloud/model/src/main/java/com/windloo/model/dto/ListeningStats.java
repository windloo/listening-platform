package com.windloo.model.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;
@Data @AllArgsConstructor
public class ListeningStats {
    private long categoryCount;
    private long albumCount;
    private long episodeCount;
    private List<CategoryStat> categories;
}