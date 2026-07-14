// package com.trademind.stock.dto;

// import lombok.*;

// import java.math.BigDecimal;
// import java.util.UUID;

// @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
// public class StockSummaryResponse {
//     private UUID id;
//     private String symbol;
//     private String companyName;
//     private String sector;
//     private BigDecimal lastPrice;
//     private BigDecimal dayChangePct;
//     private Long volume;
// }


package com.trademind.stock.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockSummaryResponse {

    private UUID id;

    private String symbol;

    private String companyName;

    private String exchange;

    private String country;

    private String currency;

    private String sector;

    private BigDecimal lastPrice;

    private BigDecimal dayChangePct;

    private Long volume;
}
