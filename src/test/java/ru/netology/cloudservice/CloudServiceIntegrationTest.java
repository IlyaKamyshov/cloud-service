package ru.netology.cloudservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.netology.cloudservice.dto.*;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CloudServiceIntegrationTest {

    @Autowired
    TestRestTemplate restTemplate;

    @LocalServerPort
    private int appPort;

    static List<String> fileList = new ArrayList<>();

    @Container
    @ServiceConnection
    static MariaDBContainer mariaDBContainer =
            new MariaDBContainer<>("mariadb:latest").withDatabaseName("cloudservice");

    @BeforeAll
    public static void setUp() {
        mariaDBContainer.start();
    }

    @Test
    @Order(1)
    public void databaseIsRunningTest() {
        assertTrue(mariaDBContainer.isRunning());
    }

    @Test
    @Order(2)
    public void loginLogoutTest() throws URISyntaxException {

        LoginRequestDTO loginRequestDTO = new LoginRequestDTO("admin", "passw0rd");

        LoginResponseDTO loginResponseDTO = restTemplate.postForObject(
                "http://localhost:" + appPort + "/login", loginRequestDTO, LoginResponseDTO.class);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("auth-token", "Bearer " + loginResponseDTO.token() + "WRONG!!!");

        ResponseEntity<String> response = restTemplate
                .exchange(RequestEntity
                        .post(new URI("http://localhost:" + appPort + "/logout"))
                        .headers(headers).build(), String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());

        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("auth-token", "Bearer " + loginResponseDTO.token());

        response = restTemplate.exchange(RequestEntity
                .post(new URI("http://localhost:" + appPort + "/logout"))
                .headers(headers)
                .build(), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

    }

    @Test
    @Order(3)
    public void uploadFileTest() throws URISyntaxException, JsonProcessingException {

        LoginRequestDTO loginRequestDTO = new LoginRequestDTO("user", "password");

        LoginResponseDTO loginResponseDTO = restTemplate.postForObject(
                "http://localhost:" + appPort + "/login", loginRequestDTO, LoginResponseDTO.class);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.add("auth-token", "Bearer " + loginResponseDTO.token());

        Path file = createFile();
        String fileName = file.getFileName().toString();

        fileList.add(fileName);

        File uploadFile = file.toFile();
        FileSystemResource fileSystemResource = new FileSystemResource(uploadFile);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileSystemResource);

        ResponseEntity<String> response = restTemplate.exchange(RequestEntity
                .post(new URI("http://localhost:" + appPort + "/file?filename=" + fileName))
                .headers(headers)
                .body(body), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        response = restTemplate.exchange(RequestEntity
                .post(new URI("http://localhost:" + appPort + "/file?filename=" + fileName))
                .headers(headers)
                .body(body), String.class);

        ErrorDTO errorJson = new ObjectMapper().readValue(response.getBody(), ErrorDTO.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Файл " + fileName + " уже загружен в облако", errorJson.message());

        body = new LinkedMultiValueMap<>();
        body.add("file", null);

        response = restTemplate.exchange(RequestEntity
                .post(new URI("http://localhost:" + appPort + "/file?filename=" + fileName))
                .headers(headers)
                .body(body), String.class);

        errorJson = new ObjectMapper().readValue(response.getBody(), ErrorDTO.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Файл отсутствует в запросе", errorJson.message());

        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("auth-token", "Bearer " + loginResponseDTO.token());

        response = restTemplate.exchange(RequestEntity
                .post(new URI("http://localhost:" + appPort + "/logout"))
                .headers(headers)
                .build(), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

    }


    @Test
    @Order(4)
    public void listTest() throws URISyntaxException {

        LoginRequestDTO loginRequestDTO = new LoginRequestDTO("user", "password");

        LoginResponseDTO loginResponseDTO = restTemplate.postForObject(
                "http://localhost:" + appPort + "/login", loginRequestDTO, LoginResponseDTO.class);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.add("auth-token", "Bearer " + loginResponseDTO.token());

        Path file = createFile();
        String fileName = file.getFileName().toString();

        fileList.add(fileName);

        File uploadFile = file.toFile();
        FileSystemResource fileSystemResource = new FileSystemResource(uploadFile);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileSystemResource);

        restTemplate.exchange(RequestEntity
                .post(new URI("http://localhost:" + appPort + "/file?filename=" + fileName))
                .headers(headers)
                .body(body), String.class);

        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("auth-token", "Bearer " + loginResponseDTO.token());

        ResponseEntity<List<FileInfoDTO>> responseList = restTemplate.exchange(RequestEntity
                .get(new URI("http://localhost:" + appPort + "/list?limit=50"))
                .headers(headers)
                .build(), new ParameterizedTypeReference<List<FileInfoDTO>>() {
        });

        List<String> responseFileList = responseList
                .getBody()
                .stream()
                .map(FileInfoDTO::fileName)
                .collect(Collectors.toList());

        Collections.sort(fileList);
        Collections.sort(responseFileList);

        assertEquals(fileList, responseFileList);

        headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.add("auth-token", "Bearer " + loginResponseDTO.token());

        file = createFile();
        fileName = file.getFileName().toString();

        uploadFile = file.toFile();
        fileSystemResource = new FileSystemResource(uploadFile);

        body = new LinkedMultiValueMap<>();
        body.add("file", fileSystemResource);

        restTemplate.exchange(RequestEntity
                .post(new URI("http://localhost:" + appPort + "/file?filename=" + fileName))
                .headers(headers)
                .body(body), String.class);

        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("auth-token", "Bearer " + loginResponseDTO.token());

        responseList = restTemplate.exchange(RequestEntity
                .get(new URI("http://localhost:" + appPort + "/list?limit=50"))
                .headers(headers)
                .build(), new ParameterizedTypeReference<List<FileInfoDTO>>() {
        });

        responseFileList = responseList
                .getBody()
                .stream()
                .map(FileInfoDTO::fileName)
                .collect(Collectors.toList());

        Collections.sort(responseFileList);

        assertNotEquals(fileList, responseFileList);

        fileList.add(fileName);

        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("auth-token", "Bearer " + loginResponseDTO.token());

        ResponseEntity<String> response = restTemplate.exchange(RequestEntity
                .post(new URI("http://localhost:" + appPort + "/logout"))
                .headers(headers)
                .build(), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

    }

    @Test
    @Order(5)
    public void updateFileTest() throws URISyntaxException, JsonProcessingException {

        LoginRequestDTO loginRequestDTO = new LoginRequestDTO("user", "password");

        LoginResponseDTO loginResponseDTO = restTemplate.postForObject(
                "http://localhost:" + appPort + "/login", loginRequestDTO, LoginResponseDTO.class);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.add("auth-token", "Bearer " + loginResponseDTO.token());

        Path file = createFile();
        String fileName = file.getFileName().toString();

        fileList.add(fileName);

        File uploadFile = file.toFile();
        FileSystemResource fileSystemResource = new FileSystemResource(uploadFile);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileSystemResource);

        restTemplate.exchange(RequestEntity
                .post(new URI("http://localhost:" + appPort + "/file?filename=" + fileName))
                .headers(headers)
                .body(body), String.class);

        int randomIndex = (int) (Math.random() * fileList.size());
        String randomFileNameFromList = fileList.get(randomIndex);

        FileRenameDTO fileRenameDTO = new FileRenameDTO("new-filename.txt");

        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("auth-token", "Bearer " + loginResponseDTO.token());

        ResponseEntity<String> response = restTemplate.exchange(RequestEntity
                .put(new URI("http://localhost:" + appPort + "/file?filename="
                        + randomFileNameFromList + "WRONG"))
                .headers(headers)
                .body(fileRenameDTO), String.class);

        ErrorDTO errorJson = new ObjectMapper().readValue(response.getBody(), ErrorDTO.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Файл " + randomFileNameFromList + "WRONG не загружен в облако", errorJson.message());

        response = restTemplate.exchange(RequestEntity
                .put(new URI("http://localhost:" + appPort + "/file?filename="
                        + randomFileNameFromList))
                .headers(headers)
                .body(fileRenameDTO), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        fileList.set(randomIndex, "new-filename.txt");

        ResponseEntity<List<FileInfoDTO>> responseList = restTemplate.exchange(RequestEntity
                .get(new URI("http://localhost:" + appPort + "/list?limit=50"))
                .headers(headers)
                .build(), new ParameterizedTypeReference<List<FileInfoDTO>>() {
        });

        List<String> responseFileList = responseList
                .getBody()
                .stream()
                .map(FileInfoDTO::fileName)
                .collect(Collectors.toList());

        Collections.sort(fileList);
        Collections.sort(responseFileList);

        assertEquals(fileList, responseFileList);

        response = restTemplate.exchange(RequestEntity
                .post(new URI("http://localhost:" + appPort + "/logout"))
                .headers(headers)
                .build(), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

    }

    @Test
    @Order(6)
    public void downloadFileTest() throws URISyntaxException, IOException {

        LoginRequestDTO loginRequestDTO = new LoginRequestDTO("user", "password");

        LoginResponseDTO loginResponseDTO = restTemplate.postForObject(
                "http://localhost:" + appPort + "/login", loginRequestDTO, LoginResponseDTO.class);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.add("auth-token", "Bearer " + loginResponseDTO.token());

        Path file = createFile();
        String fileName = file.getFileName().toString();

        fileList.add(fileName);

        File uploadFile = file.toFile();
        FileSystemResource fileSystemResource = new FileSystemResource(uploadFile);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileSystemResource);

        restTemplate.exchange(RequestEntity
                .post(new URI("http://localhost:" + appPort + "/file?filename=" + fileName))
                .headers(headers)
                .body(body), String.class);

        int randomIndex = (int) (Math.random() * fileList.size());
        String randomFileNameFromList = fileList.get(randomIndex);

        headers = new HttpHeaders();
        headers.add("auth-token", "Bearer " + loginResponseDTO.token());

        ResponseEntity<String> wrongResponse = restTemplate.exchange(RequestEntity
                .get(new URI("http://localhost:" + appPort + "/file?filename=WRONGFILENAME"))
                .headers(headers)
                .build(), String.class);

        assertEquals(HttpStatus.BAD_REQUEST, wrongResponse.getStatusCode());

        headers.setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM));

        ResponseEntity<byte[]> response = restTemplate.exchange(RequestEntity
                .get(new URI("http://localhost:" + appPort + "/file?filename=" + randomFileNameFromList))
                .headers(headers)
                .build(), byte[].class);

        byte[] fileBody = response.getBody();

        Files.write(Paths.get("./storage/" + randomFileNameFromList), fileBody, StandardOpenOption.CREATE);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(fileBody);

        ResponseEntity<String> responseLogout = restTemplate.exchange(RequestEntity
                .post(new URI("http://localhost:" + appPort + "/logout"))
                .headers(headers)
                .build(), String.class);

        assertEquals(HttpStatus.OK, responseLogout.getStatusCode());

    }

    @Test
    @Order(7)
    public void deleteFileTest() throws URISyntaxException {

        LoginRequestDTO loginRequestDTO = new LoginRequestDTO("user", "password");

        LoginResponseDTO loginResponseDTO = restTemplate.postForObject(
                "http://localhost:" + appPort + "/login", loginRequestDTO, LoginResponseDTO.class);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.add("auth-token", "Bearer " + loginResponseDTO.token());

        Path file = createFile();
        String fileName = file.getFileName().toString();

        fileList.add(fileName);

        File uploadFile = file.toFile();
        FileSystemResource fileSystemResource = new FileSystemResource(uploadFile);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileSystemResource);

        restTemplate.exchange(RequestEntity
                .post(new URI("http://localhost:" + appPort + "/file?filename=" + fileName))
                .headers(headers)
                .body(body), String.class);

        int randomIndex = (int) (Math.random() * fileList.size());
        String randomFileNameFromList = fileList.get(randomIndex);

        headers = new HttpHeaders();
        headers.add("auth-token", "Bearer " + loginResponseDTO.token());

        ResponseEntity<String> response = restTemplate.exchange(RequestEntity
                .delete(new URI("http://localhost:" + appPort + "/file?filename=WRONGFILENAME"))
                .headers(headers)
                .build(), String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        response = restTemplate.exchange(RequestEntity
                .delete(new URI("http://localhost:" + appPort + "/file?filename=" + randomFileNameFromList))
                .headers(headers)
                .build(), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        fileList.remove(randomIndex);

        ResponseEntity<List<FileInfoDTO>> responseList = restTemplate.exchange(RequestEntity
                .get(new URI("http://localhost:" + appPort + "/list?limit=50"))
                .headers(headers)
                .build(), new ParameterizedTypeReference<List<FileInfoDTO>>() {
        });

        List<String> responseFileList = responseList
                .getBody()
                .stream()
                .map(FileInfoDTO::fileName)
                .collect(Collectors.toList());

        Collections.sort(fileList);
        Collections.sort(responseFileList);

        assertEquals(fileList, responseFileList);

        response = restTemplate.exchange(RequestEntity
                .post(new URI("http://localhost:" + appPort + "/logout"))
                .headers(headers)
                .build(), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

    }

    private Path createFile() {

        try {

            Path file = Files.createTempFile("test-", ".txt");
            String absolutePath = file.toString();

            PrintWriter writer = new PrintWriter(new FileWriter(absolutePath, true));
            writer.append("[")
                    .append(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")))
                    .append(" ")
                    .append(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.nnn")))
                    .append("]\t");
            writer.flush();

            return file;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }

}
