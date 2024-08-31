package ru.netology.cloudservice.repository;

import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.netology.cloudservice.entity.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {

    UserEntity findByLogin(@NotBlank String login);

}