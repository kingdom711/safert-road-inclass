package com.jinsung.safety_road_inclass;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import com.jinsung.safety_road_inclass.domain.auth.repository.UserRepository;
import com.jinsung.safety_road_inclass.domain.team.repository.TeamRepository;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:postgresql://aws-1-ap-south-1.pooler.supabase.com:6543/postgres?prepareThreshold=0",
    "spring.datasource.username=postgres.uuzquwhlawldakidofee",
    "spring.datasource.password=5vODY1gvc67wbaTt",
    "spring.datasource.driver-class-name=org.postgresql.Driver",
    "spring.jpa.hibernate.ddl-auto=update",
    "spring.jpa.show-sql=true",
    "spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect",
    "spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation=true"
})
public class TempDbTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Test
    public void testDbConnection() {
        System.out.println("========== TESTING USER REPOSITORY ==========");
        try {
            userRepository.findById(1L);
            System.out.println("USER REPOSITORY OK");
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("========== TESTING TEAM REPOSITORY ==========");
        try {
            teamRepository.findBySiteNameOrderByNameAsc("test");
            System.out.println("TEAM REPOSITORY OK");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
