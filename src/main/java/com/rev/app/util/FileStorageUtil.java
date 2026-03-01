package com.rev.app.util;

import com.rev.app.exception.CustomException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Component
public class FileStorageUtil {

    private static final Logger logger = LogManager.getLogger(FileStorageUtil.class);

    private final Path audioStoragePath;
    private final Path imageStoragePath;

    public FileStorageUtil(
            @Value("${app.file.audio-dir}") String audioDir,
            @Value("${app.file.image-dir}") String imageDir) {
        this.audioStoragePath = Paths.get(audioDir).toAbsolutePath().normalize();
        this.imageStoragePath = Paths.get(imageDir).toAbsolutePath().normalize();
        createDirectories();
    }

    public String storeAudioFile(MultipartFile file) {
        return storeFile(file, audioStoragePath, "audio");
    }

    public String storeImageFile(MultipartFile file) {
        return storeFile(file, imageStoragePath, "images");
    }

    public void deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath).toAbsolutePath().normalize();
            Files.deleteIfExists(path);
            logger.info("Deleted file: {}", path);
        } catch (IOException e) {
            logger.error("Failed to delete file: {}", filePath, e);
        }
    }

    private String storeFile(MultipartFile file, Path storageLocation, String subDir) {
        if (file == null || file.isEmpty()) {
            throw new CustomException("File is empty or missing", HttpStatus.BAD_REQUEST);
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String storedFilename = UUID.randomUUID().toString() + extension;
        try {
            Path targetLocation = storageLocation.resolve(storedFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            logger.info("Stored file: {}", targetLocation);
            return "/uploads/" + subDir + "/" + storedFilename;
        } catch (IOException e) {
            logger.error("Failed to store file: {}", originalFilename, e);
            throw new CustomException("Failed to store file", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void createDirectories() {
        try {
            Files.createDirectories(audioStoragePath);
            Files.createDirectories(imageStoragePath);
        } catch (IOException e) {
            logger.error("Failed to create upload directories", e);
            throw new CustomException("Could not create upload directories", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
