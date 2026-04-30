package com.marques.estoque.repository.projection;

import java.math.BigDecimal;

public interface DebtorSummaryProjection {
    Long getTotalDebtors();
    BigDecimal getTotalValue();
}