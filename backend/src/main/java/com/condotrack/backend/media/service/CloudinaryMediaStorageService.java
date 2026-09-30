package com.condotrack.backend.media.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CloudinaryMediaStorageService {

    private final Cloudinary cloudinary;

    public CloudinaryUploadResult upload(
            MultipartFile file,
            String folder,
            String resourceType
    ) {
        try {
            String publicId = UUID.randomUUID().toString();
            if ("raw".equals(resourceType)) {
                publicId += fileExtension(file.getOriginalFilename());
            }

            @SuppressWarnings("rawtypes")
            Map options = ObjectUtils.asMap(
                  "resource_type", resourceType,
                  "folder", folder,
                  "public_id", publicId,
                  "overwrite", false
            );

            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), options);

            return new CloudinaryUploadResult(
                    stringValue(result, "asset_id"),
                    stringValue(result, "public_id"),
                    stringValue(result, "resource_type"),
                    stringValue(result, "type"),
                    stringValue(result, "secure_url"),
                    stringValue(result, "format"),
                    longValue(result, "bytes"),
                    integerValue(result, "width"),
                    integerValue(result, "height"),
                    doubleValue(result, "duration")
            );
        } catch (IOException | RuntimeException exception) {
            throw new IllegalStateException("Cloudinary upload failed: " + safeMessage(exception), exception);
        }
    }

    public void delete(String publicId, String resourceType, String deliveryType) {
        try {
Map result;

try {
    result = cloudinary.uploader().destroy(
            publicId,
            ObjectUtils.asMap(
                    "resource_type", resourceType,
                    "type", deliveryType,
                    "invalidate", true
            )
    );
} catch (IOException e) {
    throw new IllegalStateException(
            "Failed to delete media asset from Cloudinary: " + publicId,
            e
    );
}

            String status = stringValue(result, "result");
            if (!"ok".equalsIgnoreCase(status) && !"not found".equalsIgnoreCase(status)) {
                throw new IllegalStateException("Cloudinary deletion returned result: " + status);
            }
        } catch (RuntimeException exception) {
            throw new IllegalStateException("Cloudinary deletion failed: " + safeMessage(exception), exception);
        }
    }

    private static String stringValue(Map<String, Object> result, String key) {
        Object value = result.get(key);
        return value == null ? null : value.toString();
    }

    private static long longValue(Map<String, Object> result, String key) {
        Object value = result.get(key);
        return value instanceof Number number ? number.longValue() : 0L;
    }

    private static Integer integerValue(Map<String, Object> result, String key) {
        Object value = result.get(key);
        return value instanceof Number number ? number.intValue() : null;
    }

    private static Double doubleValue(Map<String, Object> result, String key) {
        Object value = result.get(key);
        return value instanceof Number number ? number.doubleValue() : null;
    }

    private static String safeMessage(Exception exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank() ? exception.getClass().getSimpleName() : message;
    }

    private static String fileExtension(String filename) {
        if (filename == null) {
            return "";
        }
        String normalized = filename.replace('\\', '/');
        int slash = normalized.lastIndexOf('/');
        normalized = slash >= 0 ? normalized.substring(slash + 1) : normalized;
        int dot = normalized.lastIndexOf('.');
        return dot >= 0 ? normalized.substring(dot).toLowerCase() : "";
    }

    public record CloudinaryUploadResult(
            String assetId,
            String publicId,
            String resourceType,
            String deliveryType,
            String secureUrl,
            String format,
            long bytes,
            Integer width,
            Integer height,
            Double durationSeconds
    ) {
    }
}
