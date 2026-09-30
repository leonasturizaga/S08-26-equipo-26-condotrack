package com.condotrack.backend.media.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
@EnableConfigurationProperties(CloudinaryProperties.class)
public class CloudinaryConfig {

    @Bean
    @Lazy
    public Cloudinary cloudinary(CloudinaryProperties properties) {
        if (isBlank(properties.getCloudName())
                || isBlank(properties.getApiKey())
                || isBlank(properties.getApiSecret())) {
            throw new IllegalStateException(
                    "Cloudinary configuration is incomplete. Set CLOUDINARY_CLOUD_NAME, "
                            + "CLOUDINARY_API_KEY, and CLOUDINARY_API_SECRET."
            );
        }

        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", properties.getCloudName(),
                "api_key", properties.getApiKey(),
                "api_secret", properties.getApiSecret(),
                "secure", true
        ));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
