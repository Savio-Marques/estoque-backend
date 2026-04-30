package com.marques.estoque.repository;

public interface ProductSummaryProjection {
    Long getTotal();
    Long getLowStock();
    Long getNoStock();
}
