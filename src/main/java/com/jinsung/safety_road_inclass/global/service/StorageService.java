package com.jinsung.safety_road_inclass.global.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * StorageService - 파일 저장 서비스 인터페이스
 */
public interface StorageService {

    /**
     * 파일 저장
     * @param file 업로드할 파일
     * @return 저장된 파일의 경로 (UUID 기반)
     */
    String store(MultipartFile file);

    /**
     * 파일 로드
     * @param filename 파일명
     * @return Resource 객체
     */
    Resource loadAsResource(String filename);

    /**
     * 파일 삭제
     * @param filename 파일명
     */
    void delete(String filename);

    /**
     * 저장소 초기화 (디렉토리 생성)
     */
    void init();
}

