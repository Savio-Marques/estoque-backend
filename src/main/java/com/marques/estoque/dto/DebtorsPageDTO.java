package com.marques.estoque.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Getter @Setter
public class DebtorsPageDTO {
    private List<DebtorDTO> debtorDTOList;

    private SummaryDebtorsDTO summaryDebtorsDTO;
}
