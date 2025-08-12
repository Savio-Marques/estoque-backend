package com.marques.estoque.util;

import com.marques.estoque.dto.DebtorDTO;
import com.marques.estoque.model.Debtor;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DebtorMapper {

    DebtorMapper INSTANCE = Mappers.getMapper(DebtorMapper.class);

    Debtor toDebtor(DebtorDTO debtorDTO);

    DebtorDTO toDebtorDto(Debtor debtor);

    List<DebtorDTO> toDTOList(List<Debtor> debtor);

}
