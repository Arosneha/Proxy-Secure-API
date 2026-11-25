package com.example.proxy_service.Repository;

import com.example.proxy_service.Entity.ApiUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ApiUsageRepository extends JpaRepository<ApiUsage, Long> {

    Optional<ApiUsage> findByUserEmail(String userEmail);

    @Modifying
    @Query(value = """
            INSERT INTO api_usage (user_email, request_count)
            VALUES (:email, 1)
            ON CONFLICT (user_email)
            DO UPDATE SET
                request_count = api_usage.request_count + 1,
                updated_at = CURRENT_TIMESTAMP
            """, nativeQuery = true)
    void incrementRequestCount(@Param("email") String email);
}
