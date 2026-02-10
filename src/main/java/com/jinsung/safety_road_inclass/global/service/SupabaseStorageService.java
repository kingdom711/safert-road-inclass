package com.jinsung.safety_road_inclass.global.service;

import com.jinsung.safety_road_inclass.global.config.SupabaseStorageProperties;
import com.jinsung.safety_road_inclass.global.error.CustomException;
import com.jinsung.safety_road_inclass.global.error.ErrorCode;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * SupabaseStorageService - Supabase Storage REST API 구현체
 *
 * Production 환경에서 사용됩니다.
 */
@Service
@Profile("prod")
@Slf4j
public class SupabaseStorageService implements StorageService {

    private final RestTemplate restTemplate;
    private final SupabaseStorageProperties properties;

    private static final List<String> ALLOWED_EXTENSIONS =
            Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp");

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    public SupabaseStorageService(
            @Qualifier("supabaseRestTemplate") RestTemplate restTemplate,
            SupabaseStorageProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    @PostConstruct
    @Override
    public void init() {
        try {
            String url = properties.getUrl() + "/storage/v1/bucket/" + properties.getBucket();
            HttpEntity<Void> request = new HttpEntity<>(createAuthHeaders());

            try {
                restTemplate.exchange(url, HttpMethod.GET, request, String.class);
                log.info("Supabase Storage 버킷 확인 완료: {}", properties.getBucket());
            } catch (HttpClientErrorException.NotFound e) {
                createBucket();
            }
        } catch (Exception e) {
            log.warn("Supabase Storage 버킷 확인/생성 실패: {}. " +
                    "Supabase 대시보드에서 수동으로 버킷을 생성해주세요.", e.getMessage());
        }
    }

    @Override
    public String store(MultipartFile file) {
        if (file.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        originalFilename = StringUtils.cleanPath(originalFilename);

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new CustomException(ErrorCode.FILE_SIZE_EXCEEDED);
        }

        String extension = getExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            log.warn("허용되지 않은 파일 형식: {}", extension);
            throw new CustomException(ErrorCode.INVALID_FILE_TYPE);
        }

        String storedFilename = UUID.randomUUID().toString() + "." + extension;

        try {
            String uploadUrl = properties.getUrl() + "/storage/v1/object/"
                    + properties.getBucket() + "/" + storedFilename;

            HttpHeaders headers = createAuthHeaders();
            headers.setContentType(MediaType.parseMediaType(
                    file.getContentType() != null ? file.getContentType() : "application/octet-stream"));

            HttpEntity<byte[]> request = new HttpEntity<>(file.getBytes(), headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    uploadUrl, HttpMethod.POST, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("파일 업로드 완료 (Supabase): {} -> {}", originalFilename, storedFilename);
                return storedFilename;
            } else {
                log.error("Supabase 파일 업로드 실패: status={}", response.getStatusCode());
                throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
            }

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("파일 업로드 실패 (Supabase): {}", originalFilename, e);
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    @Override
    public Resource loadAsResource(String filename) {
        try {
            String downloadUrl = properties.getUrl() + "/storage/v1/object/public/"
                    + properties.getBucket() + "/" + filename;

            ResponseEntity<byte[]> response = restTemplate.getForEntity(downloadUrl, byte[].class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                byte[] body = response.getBody();
                return new ByteArrayResource(body) {
                    @Override
                    public String getFilename() {
                        return filename;
                    }
                };
            } else {
                throw new CustomException(ErrorCode.FILE_NOT_FOUND);
            }

        } catch (CustomException e) {
            throw e;
        } catch (HttpClientErrorException.NotFound e) {
            throw new CustomException(ErrorCode.FILE_NOT_FOUND);
        } catch (Exception e) {
            log.error("파일 다운로드 실패 (Supabase): {}", filename, e);
            throw new CustomException(ErrorCode.FILE_NOT_FOUND);
        }
    }

    @Override
    public void delete(String filename) {
        try {
            String deleteUrl = properties.getUrl() + "/storage/v1/object/"
                    + properties.getBucket() + "/" + filename;

            HttpEntity<Void> request = new HttpEntity<>(createAuthHeaders());
            restTemplate.exchange(deleteUrl, HttpMethod.DELETE, request, Void.class);
            log.info("파일 삭제 완료 (Supabase): {}", filename);
        } catch (Exception e) {
            log.error("파일 삭제 실패 (Supabase): {}", filename, e);
        }
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(properties.getKey());
        headers.set("apikey", properties.getKey());
        return headers;
    }

    private void createBucket() {
        String url = properties.getUrl() + "/storage/v1/bucket";
        HttpHeaders headers = createAuthHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String body = """
                {
                    "id": "%s",
                    "name": "%s",
                    "public": true,
                    "file_size_limit": 10485760,
                    "allowed_mime_types": ["image/jpeg", "image/png", "image/gif", "image/bmp", "image/webp"]
                }
                """.formatted(properties.getBucket(), properties.getBucket());

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            log.info("Supabase Storage 버킷 생성 완료: {}", properties.getBucket());
        } catch (Exception e) {
            log.error("Supabase Storage 버킷 생성 실패: {}", e.getMessage());
        }
    }

    private String getExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1);
        }
        throw new CustomException(ErrorCode.INVALID_FILE_TYPE);
    }
}
