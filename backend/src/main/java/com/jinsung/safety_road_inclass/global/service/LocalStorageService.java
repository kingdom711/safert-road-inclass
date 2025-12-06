package com.jinsung.safety_road_inclass.global.service;

import com.jinsung.safety_road_inclass.global.error.CustomException;
import com.jinsung.safety_road_inclass.global.error.ErrorCode;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * LocalStorageService - 로컬 파일 시스템 저장 구현체
 */
@Service
@Slf4j
public class LocalStorageService implements StorageService {

    private final Path rootLocation = Paths.get("uploads");

    // 허용된 이미지 확장자
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp");

    // 최대 파일 크기 (10MB)
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    @PostConstruct
    @Override
    public void init() {
        try {
            if (!Files.exists(rootLocation)) {
                Files.createDirectories(rootLocation);
                log.info("파일 저장 디렉토리 생성: {}", rootLocation.toAbsolutePath());
            }
        } catch (IOException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public String store(MultipartFile file) {
        // 1. 파일 검증
        if (file.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        originalFilename = StringUtils.cleanPath(originalFilename);
        
        // 2. 파일 크기 검증
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new CustomException(ErrorCode.FILE_SIZE_EXCEEDED);
        }

        // 3. 확장자 추출 및 검증
        String extension = getExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            log.warn("허용되지 않은 파일 형식: {}", extension);
            throw new CustomException(ErrorCode.INVALID_FILE_TYPE);
        }

        // 4. UUID 기반 파일명 생성
        String storedFilename = UUID.randomUUID().toString() + "." + extension;

        try {
            // 5. 파일 저장
            Path destinationFile = rootLocation.resolve(storedFilename).normalize().toAbsolutePath();

            // 경로 조작 방지 (Path Traversal 공격 방어)
            if (!destinationFile.getParent().equals(rootLocation.toAbsolutePath())) {
                throw new CustomException(ErrorCode.INVALID_INPUT);
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            log.info("파일 저장 완료: {} -> {}", originalFilename, storedFilename);
            return storedFilename;

        } catch (IOException e) {
            log.error("파일 저장 실패: {}", originalFilename, e);
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = rootLocation.resolve(filename).normalize();
            @SuppressWarnings("null")
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new CustomException(ErrorCode.FILE_NOT_FOUND);
            }
        } catch (MalformedURLException e) {
            throw new CustomException(ErrorCode.FILE_NOT_FOUND);
        }
    }

    @Override
    public void delete(String filename) {
        try {
            Path file = rootLocation.resolve(filename).normalize();
            Files.deleteIfExists(file);
            log.info("파일 삭제 완료: {}", filename);
        } catch (IOException e) {
            log.error("파일 삭제 실패: {}", filename, e);
        }
    }

    /**
     * 파일 확장자 추출
     */
    private String getExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1);
        }
        throw new CustomException(ErrorCode.INVALID_FILE_TYPE);
    }
}

