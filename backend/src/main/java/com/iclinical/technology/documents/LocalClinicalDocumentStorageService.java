package com.iclinical.technology.documents;

import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class LocalClinicalDocumentStorageService {

    private final ClinicalDocumentStorageProperties properties;

    public LocalClinicalDocumentStorageService(ClinicalDocumentStorageProperties properties) {
        this.properties = properties;
    }

    public StoredClinicalDocument store(UUID organizationId, UUID patientId, MultipartFile file) {
        if (!"local".equals(properties.getProvider())) {
            throw new ClinicalDocumentStorageException("Solo se permite almacenamiento local en esta iteracion");
        }

        var fileName = safeFileName(file.getOriginalFilename());
        var objectKey = Path.of(
            "clinical-documents",
            organizationId.toString(),
            patientId.toString(),
            UUID.randomUUID() + "-" + fileName
        ).toString().replace("\\", "/");
        var target = resolveObjectKey(objectKey);

        try {
            Files.createDirectories(target.getParent());
            var digest = MessageDigest.getInstance("SHA-256");
            try (var input = new DigestInputStream(file.getInputStream(), digest); var output = Files.newOutputStream(target)) {
                input.transferTo(output);
            }
            return new StoredClinicalDocument(
                fileName,
                file.getContentType(),
                Files.size(target),
                objectKey,
                HexFormat.of().formatHex(digest.digest())
            );
        } catch (IOException | NoSuchAlgorithmException exception) {
            throw new ClinicalDocumentStorageException("No fue posible almacenar el documento clinico", exception);
        }
    }

    public FileSystemResource load(String objectKey) {
        var path = resolveObjectKey(objectKey);
        if (!Files.isRegularFile(path)) {
            throw new ClinicalDocumentStorageException("El archivo fisico del documento no esta disponible");
        }
        return new FileSystemResource(path);
    }

    private Path resolveObjectKey(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            throw new ClinicalDocumentStorageException("Ruta de almacenamiento invalida");
        }
        var base = Path.of(properties.getLocalBasePath()).toAbsolutePath().normalize();
        var resolved = base.resolve(objectKey).normalize();
        if (!resolved.startsWith(base)) {
            throw new ClinicalDocumentStorageException("Ruta de almacenamiento no permitida");
        }
        return resolved;
    }

    private String safeFileName(String originalFilename) {
        var rawName = originalFilename == null || originalFilename.isBlank() ? "documento" : Path.of(originalFilename).getFileName().toString();
        var safe = rawName.replaceAll("[^A-Za-z0-9._-]", "_");
        safe = safe.replaceAll("_+", "_");
        if (safe.isBlank() || ".".equals(safe) || "..".equals(safe)) {
            return "documento";
        }
        return safe.length() > 180 ? safe.substring(safe.length() - 180) : safe;
    }
}
