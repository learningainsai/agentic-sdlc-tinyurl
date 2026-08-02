package com.example.tinyurl.repository;

import com.example.tinyurl.entity.ShortLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface LinkRepository extends JpaRepository<ShortLink, Long> {

    Optional<ShortLink> findByCode(String code);

    boolean existsByCode(String code);

    /**
     * Returns the subset of {@code codes} that already exist, in a single {@code SELECT ... IN (...)}
     * (ADR-019). Lets bulk creation check every candidate code with one round trip instead of one
     * {@code existsByCode} per item.
     */
    @Query("select s.code from ShortLink s where s.code in :codes")
    Set<String> findExistingCodes(@Param("codes") Collection<String> codes);
}
