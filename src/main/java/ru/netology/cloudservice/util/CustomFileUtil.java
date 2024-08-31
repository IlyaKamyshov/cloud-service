package ru.netology.cloudservice.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.netology.cloudservice.logger.CloudServiceLogger;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Component
public class CustomFileUtil {

    @Value("${storage.path}")
    private String storage;

    private final CloudServiceLogger logger;

    public CustomFileUtil(CloudServiceLogger logger) {
        this.logger = logger;
    }

    public void upload(byte[] fileBytes, String hash) throws IOException {

        if (!Files.isDirectory(Paths.get(storage))) {
            Files.createDirectory(Paths.get(storage));
        }

        Path file = Files.createFile(Paths.get(storage, hash));
        FileOutputStream stream = new FileOutputStream(file.toString());
        stream.write(fileBytes);

        logger.logInfo("Файл " + hash + " успешно сохранен в файловое хранилище");

    }

    public void delete(String hash) throws IOException {

        Path path = Paths.get(storage, hash);

        try {
            Files.delete(path);
        } catch (IOException e) {
            throw new IOException("Ошибка удаления файла", e);
        }

        logger.logInfo("Файл " + hash + " успешно удален из файлового хранилища");

    }

    public byte[] download(String hash) throws IOException {
        Path path = Paths.get(storage, hash);
        byte[] file = Files.readAllBytes(path);
        logger.logInfo("Файл " + hash + " успешно загружен из файлового хранилища");
        return file;
    }

    public String getHash() {
        return UUID.randomUUID().toString();
    }

}
