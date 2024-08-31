package ru.netology.cloudservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.netology.cloudservice.entity.FileEntity;

import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, String> {

    FileEntity findByFileNameAndLogin(String fileName, String login);

    List<FileEntity> findAllByLogin(String login);

}