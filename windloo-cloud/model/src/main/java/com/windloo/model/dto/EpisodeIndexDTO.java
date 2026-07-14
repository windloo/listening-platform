package com.windloo.model.dto;

import lombok.Data;
import java.util.List;

@Data
public class EpisodeIndexDTO {
    private Long id;
    private String nameChinese;
    private String nameEnglish;
    private Long albumId;
    private List<SentenceDTO> sentences;
}
