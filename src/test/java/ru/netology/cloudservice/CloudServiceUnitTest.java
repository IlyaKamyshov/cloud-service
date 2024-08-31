package ru.netology.cloudservice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import ru.netology.cloudservice.dto.FileInfoDTO;
import ru.netology.cloudservice.dto.FileRenameDTO;
import ru.netology.cloudservice.entity.FileEntity;
import ru.netology.cloudservice.exception.InvalidInputDataException;
import ru.netology.cloudservice.logger.CloudServiceLogger;
import ru.netology.cloudservice.repository.FileRepository;
import ru.netology.cloudservice.service.FileService;
import ru.netology.cloudservice.util.CustomFileUtil;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CloudServiceUnitTest {

    @Mock
    Authentication authentication;

    @Mock
    SecurityContext securityContext;

    @Mock
    FileRepository fileRepository;

    @Mock
    CustomFileUtil customFileUtil;

    @Mock
    CloudServiceLogger logger;

    @InjectMocks
    FileService fileService;

    UserDetails userDetails;
    FileEntity fileEntity;
    MockMultipartFile multipartFile;
    byte[] fileContent;
    List<FileEntity> fileEntityList;

    @BeforeEach
    public void setUp() {

        userDetails = User.builder()
                .username("user")
                .password("password")
                .roles("USER")
                .build();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        fileEntity = FileEntity.builder()
                .id(9999L)
                .fileName("test-file.txt")
                .fileHash(UUID.randomUUID().toString())
                .size(8888L)
                .createdDate(LocalDateTime.now())
                .login("user")
                .build();

        multipartFile = new MockMultipartFile(fileEntity.getFileName(), new byte[]{1, 2, 3, 4, 5});
        fileContent = new byte[]{1, 2, 3, 4, 5};

        fileEntityList = new ArrayList<>();
        fileEntityList.add(FileEntity.builder().id(10_001L).fileName("test-file-1.txt").size(8881L).build());
        fileEntityList.add(FileEntity.builder().id(10_002L).fileName("test-file-2.txt").size(8882L).build());
        fileEntityList.add(FileEntity.builder().id(10_003L).fileName("test-file-3.txt").size(8883L).build());
        fileEntityList.add(FileEntity.builder().id(10_004L).fileName("test-file-4.txt").size(8884L).build());
        fileEntityList.add(FileEntity.builder().id(10_005L).fileName("test-file-5.txt").size(8885L).build());

    }

    @AfterEach
    void setDown() {
        userDetails = null;
        fileEntity = null;
        multipartFile = null;
        fileContent = null;
    }

    @Test
    void uploadFile() throws IOException {

        when(fileRepository.findByFileNameAndLogin(any(), any())).thenReturn(null);

        fileService.uploadFile(fileEntity.getFileName(), multipartFile);

        verify(fileRepository, times(1)).save(any(FileEntity.class));
        verify(customFileUtil, times(1)).upload(eq(multipartFile.getBytes()), any());

    }

    @Test
    void uploadFileWhenFileExist() {

        when(fileRepository.findByFileNameAndLogin(any(), any())).thenReturn(fileEntity);

        Exception exception = Assertions.assertThrows(InvalidInputDataException.class,
                () -> fileService.uploadFile(fileEntity.getFileName(), multipartFile));

        assertEquals("Файл " + fileEntity.getFileName() + " уже загружен в облако", exception.getMessage());

    }

    @Test
    void uploadFileWhenFileIsNotAttached() {

        Exception exception = Assertions.assertThrows(InvalidInputDataException.class,
                () -> fileService.uploadFile(fileEntity.getFileName(), null));

        assertEquals("Файл отсутствует в запросе", exception.getMessage());

    }

    @Test
    void deleteFile() throws IOException {

        when(fileRepository.findByFileNameAndLogin(any(), any())).thenReturn(fileEntity);

        fileService.deleteFile(fileEntity.getFileName());

        verify(fileRepository, times(1)).delete(fileEntity);
        verify(customFileUtil, times(1)).delete(fileEntity.getFileHash());

    }

    @Test
    void deleteFileWhenFileDoesNotExist() {

        when(fileRepository.findByFileNameAndLogin(any(), any())).thenReturn(null);

        Exception exception = Assertions.assertThrows(InvalidInputDataException.class,
                () -> fileService.deleteFile(fileEntity.getFileName()));

        assertEquals("Файл " + fileEntity.getFileName() + " не загружен в облако", exception.getMessage());

    }

    @Test
    void downloadFile() throws IOException {

        when(fileRepository.findByFileNameAndLogin(any(), any())).thenReturn(fileEntity);
        when(customFileUtil.download(any())).thenReturn(fileContent);

        String actual = Arrays.toString(fileService.downloadFile(fileEntity.getFileName()));
        String expected = Arrays.toString(multipartFile.getBytes());

        assertEquals(expected, actual);
        verify(customFileUtil, times(1)).download(fileEntity.getFileHash());

    }

    @Test
    void downloadFileWhenFileDoesNotExist() {

        when(fileRepository.findByFileNameAndLogin(any(), any())).thenReturn(null);

        Exception exception = Assertions.assertThrows(InvalidInputDataException.class,
                () -> fileService.downloadFile(fileEntity.getFileName()));

        assertEquals("Файл " + fileEntity.getFileName() + " не загружен в облако", exception.getMessage());

    }

    @Test
    void updateFile() {

        FileRenameDTO newName = new FileRenameDTO("new-test-file-name.txt");

        when(fileRepository.findByFileNameAndLogin(any(), any())).thenReturn(fileEntity);

        fileService.updateFile(fileEntity.getFileName(), newName);

        verify(fileRepository, times(1)).save(fileEntity);
        assertEquals(fileEntity.getFileName(), newName.fileName());

    }

    @Test
    void updateFileWhenFileDoesNotExist() {

        FileRenameDTO newName = new FileRenameDTO("new-test-file-name.txt");

        when(fileRepository.findByFileNameAndLogin(any(), any())).thenReturn(null);

        Exception exception = Assertions.assertThrows(InvalidInputDataException.class,
                () -> fileService.updateFile(fileEntity.getFileName(), newName));

        assertEquals("Файл " + fileEntity.getFileName() + " не загружен в облако", exception.getMessage());

    }

    @Test
    void getFilesList() {

        when(fileRepository.findAllByLogin(userDetails.getUsername())).thenReturn(fileEntityList);

        List<FileInfoDTO> expected = new ArrayList<>();
        expected.add(new FileInfoDTO("test-file-1.txt", 8881L));
        expected.add(new FileInfoDTO("test-file-2.txt", 8882L));
        expected.add(new FileInfoDTO("test-file-3.txt", 8883L));

        List<FileInfoDTO> actual = fileService.getFilesList(3);

        verify(fileRepository, times(1)).findAllByLogin(userDetails.getUsername());
        assertEquals(expected, actual);

    }

}
