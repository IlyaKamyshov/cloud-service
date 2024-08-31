package ru.netology.cloudservice.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.netology.cloudservice.entity.UserEntity;
import ru.netology.cloudservice.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {

        UserEntity myUser = userRepository.findByLogin(userName);

        if (myUser == null) {
            throw new UsernameNotFoundException("Неизвестный пользователь");
        }

        return User.builder()
                .username(myUser.getLogin())
                .password(myUser.getPassword())
                .roles(myUser.getRole())
                .build();

    }

}