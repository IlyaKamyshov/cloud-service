package ru.netology.cloudservice.service;

import lombok.SneakyThrows;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.cloudservice.entity.FileEntity;
import ru.netology.cloudservice.exception.InvalidInputDataException;
import ru.netology.cloudservice.dto.FileInfoDTO;
import ru.netology.cloudservice.dto.FileRenameDTO;
import ru.netology.cloudservice.logger.CloudServiceLogger;
import ru.netology.cloudservice.repository.FileRepository;
import ru.netology.cloudservice.util.CustomFileUtil;


import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FileService {

    private final FileRepository fileRepository;
    private final CustomFileUtil customFileUtil;
    private final CloudServiceLogger logger;

    public FileService(FileRepository fileRepository, CustomFileUtil customFileUtil, CloudServiceLogger logger) {
        this.fileRepository = fileRepository;
        this.customFileUtil = customFileUtil;
        this.logger = logger;
    }

    @SneakyThrows
    public void uploadFile(String fileName, MultipartFile file) {

        String login = getLogin();

        if (file == null) {
            throw new InvalidInputDataException("Файл отсутствует в запросе");
        }

        if (fileRepository.findByFileNameAndLogin(fileName, login) != null) {
            throw new InvalidInputDataException(String.format("Файл %s уже загружен в облако", fileName));
        }

        byte[] fileBytes = file.getBytes();

        String hash = customFileUtil.getHash();

        customFileUtil.upload(fileBytes, hash);

        fileRepository.save(FileEntity.builder()
                .fileName(fileName)
                .fileHash(hash)
                .size(file.getSize())
                .createdDate(LocalDateTime.now())
                .login(login)
                .build());

        logger.logInfo("Информация о файле пользователя " + login + "  [имя: " + fileName + ", hash: "
                + hash + "] успешно сохранена в базе данных");

    }

    public void deleteFile(String fileName) throws IOException {

        String login = getLogin();
        FileEntity fileEntity = getFile(fileName, login);

        if (fileEntity == null) {
            throw new InvalidInputDataException(String.format("Файл %s не загружен в облако", fileName));
        }

        customFileUtil.delete(fileEntity.getFileHash());
        fileRepository.delete(fileEntity);

        logger.logInfo("Информация о файле пользователя " + login + "  [имя: " + fileName
                + "] успешно удалена из базы данных");

    }

    public byte[] downloadFile(String fileName) throws IOException {

        String login = getLogin();
        FileEntity fileEntity = getFile(fileName, login);

        if (fileEntity == null) {
            throw new InvalidInputDataException(String.format("Файл %s не загружен в облако", fileName));
        }

        String hash = fileEntity.getFileHash();

        byte[] file = customFileUtil.download(hash);

        logger.logInfo("Пользователь " + login + " успешно загрузил файл [имя: " + fileName + "]");

        return file;

    }

    public void updateFile(String fileName, FileRenameDTO newFileName) {

        String login = getLogin();
        FileEntity fileEntity = getFile(fileName, login);

        if (fileEntity == null) {
            throw new InvalidInputDataException(String.format("Файл %s не загружен в облако", fileName));
        }

        fileEntity.setFileName(newFileName.fileName());
        fileRepository.save(fileEntity);

        logger.logInfo("Пользователь " + login + " успешно переименовал файл [старое имя: " + fileName
                + ", новое имя:" + newFileName.fileName() + "]");

    }

    public List<FileInfoDTO> getFilesList(int limit) {

        String login = getLogin();

        List<FileInfoDTO> filesList = fileRepository
                .findAllByLogin(login)
                .stream()
                .limit(limit)
                .map(fileEntity -> new FileInfoDTO(fileEntity.getFileName(), fileEntity.getSize()))
                .collect(Collectors.toList());

        logger.logInfo("Пользователь " + login + " успешно получил список своих файлов");

        return filesList;

    }

    private String getLogin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ((UserDetails) authentication.getPrincipal()).getUsername();
    }

    private FileEntity getFile(String fileName, String login) {
        return fileRepository.findByFileNameAndLogin(fileName, login);
    }

}