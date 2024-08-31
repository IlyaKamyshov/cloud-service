package ru.netology.cloudservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@NoArgsConstructor
@Getter
@Setter
@Builder
@AllArgsConstructor
public class UserEntity {

    @Id
    @Column(name = "login")
    private String login;

    @Column(name = "password")
    private String password;

    @Column(name = "role")
    private String role;

}
