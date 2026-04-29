package com.jinsung.safety_road_inclass;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;

@SpringBootApplication
@EnableScheduling
public class SafetyRoadInclassApplication {

	public static void main(String[] args) {
		// SQLite 데이터베이스 저장 디렉토리 생성 (CloudType 배포 시 필요)
		File dataDir = new File("./data");
		if (!dataDir.exists()) {
			boolean created = dataDir.mkdirs();
			System.out.println("Data directory created: " + created + " at " + dataDir.getAbsolutePath());
		}

		SpringApplication.run(SafetyRoadInclassApplication.class, args);
	}

}
