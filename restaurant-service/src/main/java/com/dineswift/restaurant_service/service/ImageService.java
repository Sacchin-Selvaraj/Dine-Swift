package com.dineswift.restaurant_service.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.dineswift.restaurant_service.exception.ImageException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final Cloudinary cloudinary;

    @Value("${cloudinary.upload-folder:restaurants}")
    private String uploadFolder;

    public Map<String,Object> uploadImage(MultipartFile file) {
        Map<String,Object> uploadResponse = new HashMap<>();
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("File is empty");
            }

            Map<String, Object> uploadParams = new HashMap<>();
            uploadParams.put("folder", uploadFolder);
            uploadParams.put("resource_type", "auto");
            uploadParams.put("quality", "auto");
            uploadParams.put("fetch_format", "auto");

            Map<?,?> uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);

            String publicId = (String) uploadResult.get("public_id");
            String url = (String) uploadResult.get("url");
            String secureUrl = (String) uploadResult.get("secure_url");
            String format = (String) uploadResult.get("format");
            String resourceType = (String) uploadResult.get("resource_type");
            Long bytes = ((Number) uploadResult.get("bytes")).longValue();
            Integer width = (Integer) uploadResult.get("width");
            Integer height = (Integer) uploadResult.get("height");

            uploadResponse.put("publicId", publicId);
            uploadResponse.put("url", url);
            uploadResponse.put("secureUrl", secureUrl);
            uploadResponse.put("format", format);
            uploadResponse.put("resourceType", resourceType);
            uploadResponse.put("bytes", bytes);
            uploadResponse.put("width", width);
            uploadResponse.put("height", height);
            uploadResponse.put("isSuccessful", true);

        } catch (IOException e) {
            throw new ImageException("Image Upload Failed "+e.getMessage());
        }
        return uploadResponse;
    }
    public String generateTransformedUrl(String publicId, Map<String, Object> transformations) {
        try {
            return cloudinary.url()
                    .transformation(new Transformation()
                            .width((Integer) transformations.getOrDefault("width", 300))
                            .height((Integer) transformations.getOrDefault("height", 300))
                            .crop((String) transformations.getOrDefault("crop", "fill"))
                            .quality((String) transformations.getOrDefault("quality", "auto"))
                            .rawTransformation((String) transformations.getOrDefault("format", "auto")))
                    .generate(publicId);
        } catch (Exception e) {
            throw new ImageException("Failed to generate transformed URL "+e.getMessage());
        }
    }

    public void deleteImage(String publicId) {
        try {
            Map<?, ?> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());

            String deletionResult = (String) result.get("result");
            if ("ok".equals(deletionResult)) {

            } else {
                throw new ImageException("Failed to delete image from Cloudinary: " + deletionResult);
            }
        } catch (IOException e) {
            throw new ImageException("Failed to delete image from Cloudinary " + e.getMessage());
        }
    }
}
