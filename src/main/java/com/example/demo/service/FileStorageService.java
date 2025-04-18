package com.example.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    @Value("${file.upload-dir}")
    private String uploadDirString;

    private Path uploadPath;

    @PostConstruct
    public void init() {
        try {
            uploadPath = Paths.get(uploadDirString).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);
            logger.info("Upload directory initialized at: {}", uploadPath.toString());
        } catch (Exception ex) {
            logger.error("Could not create the upload directory: {}", uploadDirString, ex);
            throw new RuntimeException("Could not initialize storage location", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Failed to store empty file.");
        }
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = "";
        try {
            if (originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf(".")).toLowerCase();
            }
            // Basic check for allowed extensions (can be more robust)
            if (!fileExtension.matches(".(jpg|jpeg|png|gif)$")) {
                throw new RuntimeException("Invalid file type: " + fileExtension);
            }
            if (originalFileName.contains("..")) {
                throw new RuntimeException("Cannot store file with relative path outside current directory " + originalFileName);
            }

            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
            Path targetLocation = this.uploadPath.resolve(uniqueFileName);

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
                logger.info("Stored file {} successfully at {}", uniqueFileName, targetLocation);
                // Return just the filename, the URL path will be constructed using /uploads/ prefix
                return uniqueFileName;
            }
        } catch (IOException ex) {
            logger.error("Could not store file {}. Please try again!", originalFileName, ex);
            throw new RuntimeException("Could not store file " + originalFileName + ". Please try again!", ex);
        }
    }

    // Optional: Add methods to load or delete files from the filesystem
    // public org.springframework.core.io.Resource loadFileAsResource(String fileName) { ... }
    // public void deleteFile(String fileName) { ... }
}