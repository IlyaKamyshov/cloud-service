package ru.netology.cloudservice.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.cloudservice.dto.FileInfoDTO;
import ru.netology.cloudservice.dto.FileRenameDTO;
import ru.netology.cloudservice.service.FileService;

import java.io.IOException;
import java.util.List;

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
        return fileService.getFilesList(limit);
    }

    @PostMapping("/file")
    @ResponseStatus(HttpStatus.OK)
    public void uploadFile(@RequestParam("filename") String fileName, @RequestBody MultipartFile file) {
        fileService.uploadFile(fileName, file);
    }

    @DeleteMapping("/file")
    @ResponseStatus(HttpStatus.OK)
    public void deleteFile(@RequestParam("filename")  String fileName) throws IOException {
        fileService.deleteFile(fileName);
    }

    @GetMapping("/file")
    public ResponseEntity<byte[]> downloadFile(@RequestParam("filename") String fileName) throws IOException {
        return ResponseEntity.ok()
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(fileService.downloadFile(fileName));
    }

    @PutMapping("/file")
    @ResponseStatus(HttpStatus.OK)
    public void updateFile(@RequestParam("filename") String fileName,
                           @RequestBody FileRenameDTO newFileName) {
        fileService.updateFile(fileName, newFileName);
    }

}