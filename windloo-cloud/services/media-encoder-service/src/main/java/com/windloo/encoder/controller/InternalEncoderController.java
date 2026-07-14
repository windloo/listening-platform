package com.windloo.encoder.controller;
import com.windloo.encoder.service.EncoderService;
import com.windloo.model.dto.SubmitEncoderReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/encoder")
public class InternalEncoderController {
    @Autowired EncoderService service;
    @PostMapping("/submit")
    public void submit(@RequestBody SubmitEncoderReq req) {
        service.submit(req.episodeId(), req.sourceUrl(), req.outputFormat());
    }
}