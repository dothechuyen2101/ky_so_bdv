package com.bidv.t24r.signing.storage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HexFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LocalFileStorageService {
    private final Path rootPath;

    public LocalFileStorageService(@Value("${app.storage.root-path}") String rootPath) {
        this.rootPath = Path.of(rootPath).toAbsolutePath().normalize();
    }

    public StoredFile store(
            String category,
            Instant transactionDate,
            String documentType,
            String filename,
            byte[] content,
            String mimeType
    ) {
        try {
            LocalDate date = transactionDate == null
                    ? LocalDate.now(ZoneOffset.UTC)
                    : transactionDate.atOffset(ZoneOffset.UTC).toLocalDate();
            Path relative = Path.of(
                    safeSegment(category),
                    String.valueOf(date.getYear()),
                    "%02d".formatted(date.getMonthValue()),
                    "%02d".formatted(date.getDayOfMonth()),
                    safeSegment(documentType),
                    safeFilename(filename)
            );
            Path target = resolve(relative.toString());
            Files.createDirectories(target.getParent());
            Files.write(target, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return new StoredFile(toUnixPath(relative), sha256(content), content.length, mimeType);
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot store document file", ex);
        }
    }

    public byte[] readVerified(String relativePath, String expectedHash) {
        try {
            Path file = resolve(relativePath);
            if (!Files.exists(file)) {
                throw new StorageIntegrityException("Stored file does not exist: " + relativePath);
            }
            byte[] bytes = Files.readAllBytes(file);
            String actualHash = sha256(bytes);
            if (expectedHash != null && !expectedHash.equalsIgnoreCase(actualHash)) {
                throw new StorageIntegrityException("Stored file hash mismatch for " + relativePath);
            }
            return bytes;
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot read stored document file", ex);
        }
    }

    public boolean exists(String relativePath) {
        return relativePath != null && Files.exists(resolve(relativePath));
    }

    public boolean hashMatches(String relativePath, String expectedHash) {
        if (relativePath == null || expectedHash == null || !exists(relativePath)) {
            return false;
        }
        try {
            return expectedHash.equalsIgnoreCase(sha256(Files.readAllBytes(resolve(relativePath))));
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot verify stored document file", ex);
        }
    }

    public String sha256(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(bytes));
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot calculate SHA-256", ex);
        }
    }

    public String sha256(String value) {
        return sha256(value.getBytes(StandardCharsets.UTF_8));
    }

    private Path resolve(String relativePath) {
        Path relative = Path.of(relativePath);
        if (relative.isAbsolute()) {
            throw new StorageIntegrityException("Absolute paths are not allowed in document metadata");
        }
        Path resolved = rootPath.resolve(relative).normalize();
        if (!resolved.startsWith(rootPath)) {
            throw new StorageIntegrityException("Invalid relative document path: " + relativePath);
        }
        return resolved;
    }

    private String toUnixPath(Path path) {
        return path.toString().replace('\\', '/');
    }

    private String safeSegment(String value) {
        if (value == null || value.isBlank()) {
            return "UNKNOWN";
        }
        return value.replaceAll("[^A-Za-z0-9._-]", "_");
    }

    private String safeFilename(String value) {
        return safeSegment(value == null ? "document.pdf" : value);
    }
}
