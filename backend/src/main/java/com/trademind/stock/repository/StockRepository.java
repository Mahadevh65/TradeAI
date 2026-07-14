package com.trademind.stock.repository;

// import com.trademind.stock.entity.Stock;
// import org.springframework.data.jpa.repository.JpaRepository;

// import java.util.List;
// import java.util.Optional;
// import java.util.UUID;

// public interface StockRepository extends JpaRepository<Stock, UUID> {
//     Optional<Stock> findBySymbolIgnoreCase(String symbol);
//     List<Stock> findTop10ByCompanyNameContainingIgnoreCaseOrSymbolContainingIgnoreCase(String name, String symbol);
//     List<Stock> findBySectorIgnoreCase(String sector);
//     List<Stock> findTop10ByOrderByDayChangePctDesc();
//     List<Stock> findTop10ByOrderByDayChangePctAsc();
// }

// package com.trademind.stock.repository;

import com.trademind.stock.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockRepository extends JpaRepository<Stock, UUID> {

    // Existing
    Optional<Stock> findBySymbolIgnoreCase(String symbol);

    List<Stock> findTop10ByCompanyNameContainingIgnoreCaseOrSymbolContainingIgnoreCase(
            String companyName,
            String symbol
    );

    List<Stock> findBySectorIgnoreCase(String sector);

    List<Stock> findTop10ByOrderByDayChangePctDesc();

    List<Stock> findTop10ByOrderByDayChangePctAsc();

    // New

    boolean existsBySymbolIgnoreCase(String symbol);

    List<Stock> findAllByOrderByCompanyNameAsc();

    List<Stock> findTop100ByOrderByMarketCapDesc();

}
