package com.windloo.listening.controller.admin;
import com.windloo.common.api.JsonResponse;
import com.windloo.listening.entity.Episode;
import com.windloo.listening.service.ListeningService;
import com.windloo.model.dto.SubmitEncoderReq;
import com.windloo.model.feign.MediaEncoderFeignClient;
import io.seata.spring.annotation.GlobalTransactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/listening/admin/episodes")
public class EpisodeAdminController {
    @Autowired ListeningService service;
    @Autowired MediaEncoderFeignClient mediaEncoderFeignClient;

    public record EpReq(@NotBlank String nameChinese, @NotBlank String nameEnglish, @NotNull Long albumId,
                        @NotBlank String audioUrl, @NotNull Double durationInSecond,
                        @NotBlank String subtitle, @NotBlank String subtitleType) {}

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public JsonResponse<List<Episode>> list(@RequestParam Long albumId) {
        return JsonResponse.ok(service.getEpisodesByAlbumAll(albumId));
    }

    /**
     * 创建单集 + 提交转码任务，两者在同一全局事务中：
     * - 单集写入 listening 库（分支事务 1）
     * - 转码任务写入 encoder 库（分支事务 2，经 Feign 调用）
     * 若任一分支失败，Seata 自动回滚所有分支。
     */
    @PostMapping
    @GlobalTransactional
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public JsonResponse<Episode> create(@RequestBody @Valid EpReq req) {
        Episode ep = service.createEpisode(req.nameChinese(), req.nameEnglish(), req.albumId(),
                req.audioUrl(), req.durationInSecond(), req.subtitle(), req.subtitleType());
        mediaEncoderFeignClient.submit(new SubmitEncoderReq(ep.getId(), req.audioUrl(), "m4a"));
        return JsonResponse.ok(ep);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public JsonResponse<Episode> update(@PathVariable Long id, @RequestBody @Valid EpReq req) {
        return JsonResponse.ok(service.updateEpisode(id, req.nameChinese(), req.nameEnglish(),
                req.audioUrl(), req.durationInSecond(), req.subtitle(), req.subtitleType()));
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public JsonResponse<Void> delete(@PathVariable Long id) { service.deleteEpisode(id); return JsonResponse.ok(); }
    @PutMapping("/sort")
    @PreAuthorize("hasRole('ADMIN')")
    public JsonResponse<Void> sort(@RequestBody List<Long> sortedIds) { service.sortEpisodes(sortedIds); return JsonResponse.ok(); }
    @PutMapping("/{id}/show")
    @PreAuthorize("hasRole('ADMIN')")
    public JsonResponse<Void> show(@PathVariable Long id) { service.showEpisode(id); return JsonResponse.ok(); }
    @PutMapping("/{id}/hide")
    @PreAuthorize("hasRole('ADMIN')")
    public JsonResponse<Void> hide(@PathVariable Long id) { service.hideEpisode(id); return JsonResponse.ok(); }

    /** 一键重建搜索索引：重新发布所有单集的索引事件 */
    @PostMapping("/reindex")
    @PreAuthorize("hasRole('ADMIN')")
    public JsonResponse<Integer> reindex() {
        return JsonResponse.ok(service.reindexAll());
    }
}