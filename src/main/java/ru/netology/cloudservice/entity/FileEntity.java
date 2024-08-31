package ru.netology.cloudservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "files")
@NoArgsConstructor
@Getter
@Setter
@Builder
@AllArgsConstructor
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    @Column(name = "file_name")
    private String fileName;
    @Column(name = "file_hash")
    private String fileHash;
    @Column(name = "file_size")
    private Long size;
    @Column(name = "created_date")
    private LocalDateTime createdDate;
    @Column(name = "file_owner")
    private String login;

}