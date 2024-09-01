package ru.netology.cloudservice.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.cloudservice.dto.FileInfoDTO;
import ru.netology.cloudservice.dto.FileRenameDTO;
import ru.netology.cloudservice.service.FileService;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/list")
    @ResponseStatus(HttpStatus.OK)
    public List<FileInfoDTO> getFilesList(@RequestParam int limit) {
        return fileService.getFilesList(getLogin(), limit)
                .stream()
                .map(fileEntity -> new FileInfoDTO(fileEntity.getFileName(), fileEntity.getSize()))
                .collect(Collectors.toList());
    }

    @PostMapping("/file")
    @ResponseStatus(HttpStatus.OK)
    public void uploadFile(@RequestParam("filename") String fileName, @RequestBody MultipartFile file)
            throws IOException {
        fileService.uploadFile(getLogin(), fileName, file);
    }

    @DeleteMapping("/file")
    @ResponseStatus(HttpStatus.OK)
    public void deleteFile(@RequestParam("filename") String fileName) throws IOException {
        fileService.deleteFile(getLogin(), fileName);
    }

    @GetMapping("/file")
    public ResponseEntity<byte[]> downloadFile(@RequestParam("filename") String fileName) throws IOException {
        return ResponseEntity.ok()
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(fileService.downloadFile(getLogin(), fileName));
    }

    @PutMapping("/file")
    @ResponseStatus(HttpStatus.OK)
    public void updateFile(@RequestParam("filename") String fileName,
                           @RequestBody FileRenameDTO newFileName) {
        fileService.updateFile(getLogin(), fileName, newFileName);
    }

    private String getLogin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ((UserDetails) authentication.getPrincipal()).getUsername();
    }

}