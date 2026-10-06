package com.expensetracker.analyticsservice.dto;

import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryBreakdown implements Serializable {
    private String category;
    private BigDecimal amount;
    private Double percentage;
}
