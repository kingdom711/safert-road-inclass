package com.jinsung.safety_road_inclass.global.controller;

import com.jinsung.safety_road_inclass.global.service.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * FileController - 로컬(dev) 파일 다운로드 API.
 * prod에서는 Supabase Public CDN URL을 클라이언트가 직접 호출하므로 비활성화.
 */
@Tag(name = "File", description = "파일 관리 API (dev only)")
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@Profile("dev")
@Slf4j
public class FileController {

    private final StorageService storageService;

    @Operation(summary = "파일 다운로드", description = "저장된 파일을 다운로드합니다.")
    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
        log.debug("파일 다운로드 요청: {}", filename);
        
        Resource resource = storageService.loadAsResource(filename);

        @SuppressWarnings("null")
        ResponseEntity<Resource> response = ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
        
        return response;
    }
}

