package com.marques.estoque.repository.projection;

public interface ProductSummaryProjection {
    Long getTotal();
    Long getLowStock();
    Long getNoStock();
}
