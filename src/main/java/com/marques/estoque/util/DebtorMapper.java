package com.marques.estoque.util;

import com.marques.estoque.dto.DebtorDTO;
import com.marques.estoque.model.Debtor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DebtorMapper {

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "paid", ignore = true)
    @Mapping(target = "settledAt", ignore = true)
    Debtor toDebtor(DebtorDTO debtorDTO);

    @Mapping(target = "transactionNote", ignore = true)
    @Mapping(target = "stockItems", ignore = true)
    @Mapping(target = "deductFromStock", ignore = true)
    DebtorDTO toDTO(Debtor debtor);

    List<DebtorDTO> toDTOList(List<Debtor> debtor);
}
