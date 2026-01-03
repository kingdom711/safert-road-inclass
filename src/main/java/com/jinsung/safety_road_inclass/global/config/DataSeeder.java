package com.jinsung.safety_road_inclass.global.config;

import com.jinsung.safety_road_inclass.domain.auth.entity.Role;
import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.domain.auth.repository.UserRepository;
import com.jinsung.safety_road_inclass.domain.template.entity.ChecklistTemplate;
import com.jinsung.safety_road_inclass.domain.template.entity.TemplateItem;
import com.jinsung.safety_road_inclass.domain.template.entity.WorkType;
import com.jinsung.safety_road_inclass.domain.template.repository.ChecklistTemplateRepository;
import com.jinsung.safety_road_inclass.domain.template.repository.WorkTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * DataSeeder - 개발 환경 초기 데이터 시딩
 * 
 * dev 프로파일에서만 실행됩니다.
 */
@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
@Order(1000) // 다른 초기화 작업 이후에 실행되도록
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final WorkTypeRepository workTypeRepository;
    private final ChecklistTemplateRepository templateRepository;

    @Override
    public void run(String... args) {
        try {
            // 테이블이 생성되었는지 확인
            long userCount = userRepository.count();
            if (userCount > 0) {
                log.info("데이터가 이미 존재합니다. 시딩을 건너뜁니다.");
                return;
            }

            log.info("=== 초기 데이터 시딩 시작 ===");
            seedUsers();
            seedTemplates();
            log.info("=== 초기 데이터 시딩 완료 ===");
        } catch (Exception e) {
            log.error("데이터 시딩 중 오류 발생: {}", e.getMessage(), e);
            // 테이블이 아직 생성되지 않은 경우 무시하고 계속 진행
            if (e.getMessage() != null && e.getMessage().contains("Table") && e.getMessage().contains("not found")) {
                log.warn("테이블이 아직 생성되지 않았습니다. 다음 실행 시 시딩됩니다.");
            } else {
                throw e; // 다른 오류는 다시 던짐
            }
        }
    }

    private void seedUsers() {
        log.info("사용자 데이터 시딩 시작...");
        
        User worker1 = User.builder()
            .username("worker1").password("password123").role(Role.ROLE_WORKER)
            .name("김대리").email("worker1@safetyroad.com").build();
        worker1.encodePassword(passwordEncoder);
        userRepository.save(worker1);

        User worker2 = User.builder()
            .username("worker2").password("password123").role(Role.ROLE_WORKER)
            .name("이사원").email("worker2@safetyroad.com").build();
        worker2.encodePassword(passwordEncoder);
        userRepository.save(worker2);

        User supervisor = User.builder()
            .username("supervisor").password("password123").role(Role.ROLE_SUPERVISOR)
            .name("박소장").email("supervisor@safetyroad.com").build();
        supervisor.encodePassword(passwordEncoder);
        userRepository.save(supervisor);

        User manager = User.builder()
            .username("manager").password("password123").role(Role.ROLE_SAFETY_MANAGER)
            .name("이팀장").email("manager@safetyroad.com").build();
        manager.encodePassword(passwordEncoder);
        userRepository.save(manager);

        log.info("사용자 {}명 생성 완료", userRepository.count());
    }

    private void seedTemplates() {
        log.info("템플릿 데이터 시딩 시작...");

        // 1. 사다리 작업
        WorkType ladder = workTypeRepository.save(
            WorkType.builder().name("사다리 작업").description("이동식 사다리 안전점검").build()
        );
        ChecklistTemplate ladderTemplate = ChecklistTemplate.builder()
            .workType(ladder).title("사다리 안전점검표").description("사다리 작업 전 안전점검").build();
        ladderTemplate.addItem(TemplateItem.builder().itemOrder(1).content("사다리 발판이 파손되지 않았는가?").category("구조").build());
        ladderTemplate.addItem(TemplateItem.builder().itemOrder(2).content("사다리 미끄럼 방지 조치가 되어있는가?").category("안전조치").build());
        ladderTemplate.addItem(TemplateItem.builder().itemOrder(3).content("작업 시 안전모를 착용하였는가?").category("보호구").build());
        templateRepository.save(ladderTemplate);

        // 2. 고소작업대
        WorkType aerial = workTypeRepository.save(
            WorkType.builder().name("고소작업대 작업").description("고소작업대 운용 점검").build()
        );
        ChecklistTemplate aerialTemplate = ChecklistTemplate.builder()
            .workType(aerial).title("고소작업대 점검표").description("고소작업대 작업 전 점검").build();
        aerialTemplate.addItem(TemplateItem.builder().itemOrder(1).content("작업대 작동 상태는 정상인가?").category("장비").build());
        aerialTemplate.addItem(TemplateItem.builder().itemOrder(2).content("안전벨트를 착용하였는가?").category("보호구").build());
        templateRepository.save(aerialTemplate);

        // 3. 밀폐공간
        WorkType confined = workTypeRepository.save(
            WorkType.builder().name("밀폐공간 작업").description("밀폐공간 출입 전 점검").build()
        );
        ChecklistTemplate confinedTemplate = ChecklistTemplate.builder()
            .workType(confined).title("밀폐공간 출입 점검표").description("밀폐공간 작업 전 필수 점검").build();
        confinedTemplate.addItem(TemplateItem.builder().itemOrder(1).content("산소농도가 18% 이상 23.5% 이하인가?").category("환경").build());
        confinedTemplate.addItem(TemplateItem.builder().itemOrder(2).content("유해가스 농도가 기준치 이하인가?").category("환경").build());
        confinedTemplate.addItem(TemplateItem.builder().itemOrder(3).content("환기설비가 정상 작동하는가?").category("설비").build());
        templateRepository.save(confinedTemplate);

        log.info("템플릿 {}개 생성 완료", templateRepository.count());
    }
}

