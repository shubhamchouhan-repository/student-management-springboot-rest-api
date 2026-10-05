package com.example.sms.service.impl;

import com.example.sms.exception.FileStorageException;
import com.example.sms.exception.InvalidFileException;
import com.example.sms.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private static final Map<String, String> ALLOWED_CONTENT_TYPES = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp"
    );

    private final Path storageLocation;

    public FileStorageServiceImpl(
            @Value("${file.upload-dir}") String uploadDir) {

        this.storageLocation = Paths
                .get(uploadDir)
                .toAbsolutePath()
                .normalize();

        try {
            Files.createDirectories(storageLocation);

            log.info("Profile image storage directory: {}", storageLocation);

        } catch (IOException e) {
            throw new FileStorageException("Could not create file storage directory", e);
        }
    }

    // =========================================================
    // STORE
    // =========================================================

    @Override
    public String store(MultipartFile file) {

        validateFile(file);

        String contentType = file.getContentType();

        String extension = ALLOWED_CONTENT_TYPES.get(contentType);

        String fileName = UUID.randomUUID() + extension;

        Path targetLocation = storageLocation.resolve(fileName);

        try {
            Files.copy(
                    file.getInputStream(),
                    targetLocation
            );

            log.info("Profile image stored successfully: {}", fileName);

            return fileName;

        } catch (IOException e) {

            log.error("Failed to store profile image", e);

            throw new FileStorageException("Could not store profile image", e);
        }
    }

    // =========================================================
    // LOAD
    // =========================================================

    @Override
    public Resource load(String fileName) {

        try {

            Path filePath = storageLocation
                    .resolve(fileName)
                    .normalize();

            /*
             * Security check.
             *
             * Prevents paths such as:
             *
             * ../../some-important-file
             */
            if (!filePath.startsWith(storageLocation)) {
                throw new FileStorageException("Invalid file path");
            }

            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                throw new FileStorageException("Profile image not found: " + fileName);
            }

            return resource;

        } catch (MalformedURLException e) {

            throw new FileStorageException("Could not load profile image", e);
        }
    }

    // =========================================================
    // DELETE
    // =========================================================

    @Override
    public void delete(String fileName) {

        if (fileName == null || fileName.isBlank()) {
            return;
        }

        try {
            Path filePath = storageLocation
                    .resolve(fileName)
                    .normalize();

            if (!filePath.startsWith(storageLocation)) {
                throw new FileStorageException("Invalid file path");
            }

            boolean deleted = Files.deleteIfExists(filePath);

            if (deleted) {
                log.info("Profile image deleted: {}", fileName);
            } else {
                log.warn("Profile image not found: {}", fileName);
            }

        } catch (IOException e) {
            throw new FileStorageException("Could not delete profile image", e);
        }
    }

    // =========================================================
    // VALIDATION
    // =========================================================

    private void validateFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("Profile image is required");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidFileException("Profile image must not exceed 5 MB");
        }

        String contentType = file.getContentType();

        if (contentType == null || !ALLOWED_CONTENT_TYPES.containsKey(contentType)) {
            throw new InvalidFileException("Only JPG, PNG and WEBP images are allowed");
        }
    }
}