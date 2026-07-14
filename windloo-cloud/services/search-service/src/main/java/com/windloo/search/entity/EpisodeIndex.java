package com.windloo.search.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Data
@Document(indexName = "episode")
public class EpisodeIndex {
    @Id
    private Long id;
    @Field(type = FieldType.Text)
    private String nameChinese;
    @Field(type = FieldType.Text)
    private String nameEnglish;
    @Field(type = FieldType.Long)
    private Long albumId;

    /** 以 Nested 类型存储带时间戳的句子，支持按句子文本检索并保留精确定位所需的时间戳。 */
    @Field(type = FieldType.Nested)
    private List<SentenceDoc> sentences;

    @Data
    public static class SentenceDoc {
        @Field(type = FieldType.Long)
        private long startMs;
        @Field(type = FieldType.Long)
        private long endMs;
        @Field(type = FieldType.Text)
        private String text;
    }
}
