package com.marques.estoque.util;

import com.marques.estoque.dto.UserCreateDTO;
import com.marques.estoque.dto.UserResponseDTO;
import com.marques.estoque.model.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    User toUser(UserCreateDTO userCreateDTO);

    List<UserResponseDTO> toDTOList(List<User> user);

    UserCreateDTO toUserCreateDTO(User user);

    UserResponseDTO toUserResponseDTO(User user);
}
