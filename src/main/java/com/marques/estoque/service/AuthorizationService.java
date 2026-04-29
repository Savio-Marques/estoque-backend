package com.marques.estoque.service;

import com.marques.estoque.exception.NotFoundException;
import com.marques.estoque.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorizationService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        UserDetails user= userRepository.findByUsername(username);

        if (user == null) throw new NotFoundException("Nome de usuário não encotrado.");

        return user;
    }
}
