package com.grabbill.server.controller;

import com.grabbill.core.entity.Image;
import com.grabbill.core.model.storage.FileObjectType;
import com.grabbill.core.service.FileStorageService;
import com.grabbill.core.service.ImageService;
import com.grabbill.server.GrabbillServerErrorCode;
import com.grabbill.server.exception.GrabbillServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author michaellow
 */
@RestController
@RequestMapping("/ext/images")
public class ExternalImageController {

    private static final String RESOURCE_NOT_FOUND = "not_found_404.jpg";

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private ImageService imageService;


    @Transactional
    @GetMapping(value = "/{linkId}/{fileName}")
    public ResponseEntity<ByteArrayResource> loadImage(
            @PathVariable String linkId,
            @PathVariable String fileName
    ) {
        Optional<Image> imageOptional = imageService.getByLinkIdAndFileName(linkId, fileName);

        String fileType;
        byte[] bytes;
        if (!imageOptional.isPresent()) {
            fileType = "image/jpeg";
            ClassPathResource classPathResource = new ClassPathResource("static/" + RESOURCE_NOT_FOUND, this.getClass().getClassLoader());
            try {
                bytes = classPathResource.getInputStream().readAllBytes();
            } catch (IOException e) {
                throw new GrabbillServerException(GrabbillServerErrorCode.GRB0001, "Not able to load static image!");
            }

        } else {
            Image image = imageOptional.get();
            fileType = image.getFileType();

            bytes = fileStorageService.download(
                    image.getAccount(),
                    FileObjectType.IMAGES,
                    image.getImageFolder() != null ? image.getImageFolder().getName() : null,
                    image.getFilename()
            );
        }

        List<String> exposeHeaders = new ArrayList<>();
        exposeHeaders.add(HttpHeaders.CONTENT_DISPOSITION);
        HttpHeaders headers = new HttpHeaders();
        headers.setAccessControlExposeHeaders(exposeHeaders);
        headers.setContentType(MediaType.parseMediaType(fileType));

        return new ResponseEntity<>(new ByteArrayResource(bytes), headers, HttpStatus.OK);
    }

    private String buildFileStorageFileName(
            final String oriFileName,
            final Long imageId
    ) {
        return oriFileName + "_" + imageId;
    }

}
