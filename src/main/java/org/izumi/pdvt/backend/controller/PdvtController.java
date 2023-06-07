package org.izumi.pdvt.backend.controller;

import java.util.Base64;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.izumi.pdvt.backend.utils.Utils;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequiredArgsConstructor
@RestController
public class PdvtController extends AbstractController {
    private final SimpMessagingTemplate template;

    @PostMapping("/client/{code}/files/add")
    public ResponseEntity<?> upload(@PathVariable(name = "code") String code,
                                    @RequestParam("file") MultipartFile file) {
        emit(code, file, "pdvt");
        return ok();
    }

    @PostMapping("/client/{code}/files/add-and-analyze")
    public ResponseEntity<?> uploadAndAnalyze(@PathVariable(name = "code") String code,
                                              @RequestParam("file") MultipartFile file) {
        emit(code, file, "file-and-analyze");
        return ok();
    }

    private void emit(String code, MultipartFile file, String destination) {
        final String filename = filename(file);
        final String body = Utils.silently(() -> Base64.getEncoder().encodeToString(
                file.getInputStream().readAllBytes())
        );
        final String payload = filename.length() + "." + filename + body;

        template.convertAndSendToUser(code, "/ws/" + destination, payload);
    }

    private String filename(MultipartFile file) {
        return Optional.ofNullable(file.getOriginalFilename())
                .orElseGet(file::getName);
    }
}
