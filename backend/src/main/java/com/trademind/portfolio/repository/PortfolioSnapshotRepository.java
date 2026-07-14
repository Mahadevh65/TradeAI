package com.trademind.portfolio.repository;

import com.trademind.portfolio.entity.PortfolioSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PortfolioSnapshotRepository extends JpaRepository<PortfolioSnapshot, UUID> {
    List<PortfolioSnapshot> findByUserIdAndSnapshotDateBetweenOrderBySnapshotDateAsc(
            UUID userId, LocalDate from, LocalDate to);
    Optional<PortfolioSnapshot> findByUserIdAndSnapshotDate(UUID userId, LocalDate date);
    Optional<PortfolioSnapshot> findFirstByUserIdAndSnapshotDateLessThanEqualOrderBySnapshotDateDesc(
            UUID userId, LocalDate date);

    @org.springframework.data.jpa.repository.Query("select distinct h.user.id from Holding h")
    List<UUID> findDistinctUserIdsWithHoldings();
}
