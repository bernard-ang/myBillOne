package com.grabbill.server.controller;

import com.grabbill.core.entity.Account;
import com.grabbill.core.entity.Image;
import com.grabbill.core.entity.ImageFolder;
import com.grabbill.core.entity.User;
import com.grabbill.core.model.ActionType;
import com.grabbill.core.model.DomainType;
import com.grabbill.core.model.storage.FileObjectType;
import com.grabbill.core.service.AuditLogService;
import com.grabbill.core.service.FileStorageService;
import com.grabbill.core.service.ImageFolderService;
import com.grabbill.core.service.ImageService;
import com.grabbill.server.GrabbillServerErrorCode;
import com.grabbill.server.controller.request.ImageFolderRequest;
import com.grabbill.server.controller.response.GrabbillApiResponse;
import com.grabbill.server.controller.response.GrabbillServerApiVersion;
import com.grabbill.server.controller.response.payload.ImageFolderPayload;
import com.grabbill.server.controller.response.payload.ImagePayload;
import com.grabbill.server.controller.response.payload.ImagesPayload;
import com.grabbill.server.controller.response.payload.RootImageFolderPayload;
import com.grabbill.server.dto.GrabbillUserDetails;
import com.grabbill.server.exception.GrabbillServerException;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @author michaellow
 */
@RestController
@RequestMapping("/int/images")
public class ImageController extends PaymentAwareController {

    private static final List<String> SUPPORTED_IMAGE_TYPES = Arrays.asList("gif", "jpg", "jpeg", "png");
    private static final String RESOURCE_NOT_FOUND = "not_found_404.jpg";

    @Value("${image.external-link.url}")
    private String externalLinkUrl;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private ImageFolderService imageFolderService;

    @Autowired
    private ImageService imageService;


    @Transactional
    @GetMapping()
    public ResponseEntity<GrabbillApiResponse> listAll(
            @AuthenticationPrincipal GrabbillUserDetails userDetails
    ) {
        Account account = userDetails.getUser().getAccount();
        RootImageFolderPayload target = createRootImageFolderPayload(account);

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        target
                )
        );
    }

    @Transactional
    @PostMapping()
    public ResponseEntity<GrabbillApiResponse> uploadImage(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @RequestPart(name = "file") MultipartFile file,
            @RequestPart(name = "folder", required = false) String folderName
    ) {
        User user = userDetails.getUser();
        verifyPaymentStatus(user);
        Account account = user.getAccount();

        if (file.getContentType() != null && isMediaTypeSupported(file.getContentType())) {
            ImageFolder imageFolder = null;
            if (StringUtils.hasLength(folderName)) {
                imageFolder = imageFolderService.getByName(account, folderName).orElseThrow(
                        () -> new GrabbillServerException(
                                GrabbillServerErrorCode.GRB1011,
                                "Image folder with name [" + folderName + "] not found!"
                        )
                );

                List<Image> images = imageService.getByAccountAndFileNameAndImageFolderName(
                        account, file.getOriginalFilename(), imageFolder.getName());
                if (!images.isEmpty()) {
                    throw new GrabbillServerException(
                            GrabbillServerErrorCode.GRB1014,
                            "Image with name [" + file.getOriginalFilename() + "] already exists!"
                    );
                }

            } else {
                List<Image> images = imageService.getByAccountAndFileNameAndImageFolderIsNull(
                        account, file.getOriginalFilename());
                if (!images.isEmpty()) {
                    throw new GrabbillServerException(
                            GrabbillServerErrorCode.GRB1014,
                            "Image with name [" + file.getOriginalFilename() + "] already exists!"
                    );
                }
            }

            Image image = new Image();
            image.setLinkId(imageService.generateLinkId());
            image.setFilename(file.getOriginalFilename());
            image.setFileType(file.getContentType());
            image.setFileSize(file.getSize());
            if (imageFolder != null) {
                image.setImageFolder(imageFolder);
            }
            image.setAccount(account);
            Image savedImage = imageService.save(image);

            try {
                fileStorageService.upload(
                        account,
                        FileObjectType.IMAGES,
                        imageFolder != null ? imageFolder.getName() : null,
                        file.getOriginalFilename(),
                        file.getBytes()
                );
                Item item = fileStorageService.find(
                        account,
                        FileObjectType.IMAGES,
                        imageFolder != null ? imageFolder.getName() : null,
                        file.getOriginalFilename()
                );

                savedImage.setFileSize(item.size());
                savedImage = imageService.save(savedImage);

                auditLogService.log(
                        account.getId(),
                        Optional.empty(),
                        savedImage.getId(),
                        DomainType.IMAGE,
                        ActionType.UPLOAD,
                        savedImage.getFilename(),
                        userDetails.getUsername()
                );

            } catch (IOException e) {
                imageService.delete(savedImage);
                throw new GrabbillServerException(
                        GrabbillServerErrorCode.GRB0001,
                        "Image upload failed!"
                );
            }

        } else {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB1012,
                    "Image of type [" + file.getContentType() + "] is not supported"
            );
        }

        List<Image> images = StringUtils.hasLength(folderName) ?
                imageService.getByAccountAndImageFolderName(account, folderName) :
                imageService.getByAccountAndImageFolderIsNull(account);
        ImagesPayload target = new ImagesPayload();
        for (Image image : images) {
            ImagePayload targetImagePayload = ImagePayload.from(image);
            appendExternalLinkUrl(targetImagePayload);
            target.getImages().add(targetImagePayload);
        }

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        target
                )
        );
    }

    @Transactional
    @PutMapping(path = "/{imageId}")
    public ResponseEntity<GrabbillApiResponse> updateImage(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Long imageId,
            @RequestPart(name = "file") MultipartFile file,
            @RequestPart(name = "folder", required = false) String folderName
    ) {
        User user = userDetails.getUser();
        verifyPaymentStatus(user);
        Account account = user.getAccount();
        Image targetImage = imageService.getByAccountAndId(account, imageId).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB1010,
                        "Image with id [" + imageId + "] not found!"
                )
        );

        if (file.getContentType() != null && isMediaTypeSupported(file.getContentType())) {
            ImageFolder imageFolder = null;
            if (StringUtils.hasLength(folderName)) {
                imageFolder = imageFolderService.getByName(account, folderName).orElseThrow(
                        () -> new GrabbillServerException(
                                GrabbillServerErrorCode.GRB1011,
                                "Image folder with name [" + folderName + "] not found!"
                        )
                );
            }

            try {

                fileStorageService.delete(
                        account,
                        FileObjectType.IMAGES,
                        imageFolder != null ? imageFolder.getName() : null,
                        targetImage.getFilename()
                );

                fileStorageService.upload(
                        account,
                        FileObjectType.IMAGES,
                        imageFolder != null ? imageFolder.getName() : null,
                        file.getOriginalFilename(),
                        file.getBytes()
                );
                Item item = fileStorageService.find(
                        account,
                        FileObjectType.IMAGES,
                        imageFolder != null ? imageFolder.getName() : null,
                        file.getOriginalFilename()
                );

                targetImage.setFilename(file.getOriginalFilename());
                targetImage.setFileType(file.getContentType());
                targetImage.setFileSize(item.size());
                imageService.save(targetImage);

                auditLogService.log(
                        account.getId(),
                        Optional.empty(),
                        targetImage.getId(),
                        DomainType.IMAGE,
                        ActionType.UPLOAD,
                        targetImage.getFilename(),
                        userDetails.getUsername()
                );

            } catch (IOException e) {
                throw new GrabbillServerException(
                        GrabbillServerErrorCode.GRB0001,
                        "Image upload failed!"
                );
            }

        } else {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB1012,
                    "Image of type [" + file.getContentType() + "] is not supported"
            );
        }

        List<Image> images = StringUtils.hasLength(folderName) ?
                imageService.getByAccountAndImageFolderName(account, folderName) :
                imageService.getByAccountAndImageFolderIsNull(account);
        ImagesPayload target = new ImagesPayload();
        for (Image image : images) {
            ImagePayload targetImagePayload = ImagePayload.from(image);
            appendExternalLinkUrl(targetImagePayload);
            target.getImages().add(targetImagePayload);
        }

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        target
                )
        );
    }

    @Transactional
    @GetMapping(path = "/{imageId}")
    public ResponseEntity<ByteArrayResource> downloadImage(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Long imageId
    ) {
        Account account = userDetails.getUser().getAccount();
        Optional<Image> imageOptional = imageService.getByAccountAndId(account, imageId);

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
                    account,
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

    @Transactional
    @DeleteMapping(value = "/{imageId}")
    public ResponseEntity<GrabbillApiResponse> deleteImageById(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Long imageId
    ) {
        User user = userDetails.getUser();
        Account account = user.getAccount();
        Image targetImage = imageService.getByAccountAndId(account, imageId).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB1010,
                        "Image with id [" + imageId + "] not found!"
                )
        );
        ImageFolder imageFolder = targetImage.getImageFolder();

        fileStorageService.delete(
                account,
                FileObjectType.IMAGES,
                imageFolder != null ? imageFolder.getName() : null,
                targetImage.getFilename()
        );
        imageService.delete(targetImage);

        auditLogService.log(
                account.getId(),
                Optional.empty(),
                targetImage.getId(),
                DomainType.IMAGE,
                ActionType.DELETE,
                targetImage.getFilename(),
                userDetails.getUsername()
        );

        List<Image> images = imageFolder != null ?
                imageService.getByAccountAndImageFolderName(account, imageFolder.getName()) :
                imageService.getByAccountAndImageFolderIsNull(account);
        ImagesPayload target = new ImagesPayload();
        for (Image image : images) {
            ImagePayload targetImagePayload = ImagePayload.from(image);
            appendExternalLinkUrl(targetImagePayload);
            target.getImages().add(targetImagePayload);
        }

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        target
                )
        );
    }

    @Transactional
    @PostMapping(value = "/create-folder")
    public ResponseEntity<GrabbillApiResponse> createNewFolder(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @Valid @RequestBody ImageFolderRequest request
    ) {
        User user = userDetails.getUser();
        verifyPaymentStatus(user);
        Account account = user.getAccount();
        if (imageFolderService.exist(account, request.getName())) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB1013,
                    "Image folder with name [" + request.getName() + "] already exists!"
            );
        }

        ImageFolder targetFolder = new ImageFolder();
        targetFolder.setName(request.getName());
        targetFolder.setAccount(account);
        ImageFolder savedFolder = imageFolderService.save(targetFolder);

        auditLogService.log(
                account.getId(),
                Optional.empty(),
                savedFolder.getId(),
                DomainType.IMAGE_FOLDER,
                ActionType.CREATE,
                targetFolder.getName(),
                userDetails.getUsername()
        );

        RootImageFolderPayload target = createRootImageFolderPayload(account);
        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        target
                )
        );
    }

    @Transactional
    @DeleteMapping(value = "/delete-folder")
    public ResponseEntity<GrabbillApiResponse> deleteFolder(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @Valid @RequestBody ImageFolderRequest request
    ) {
        User user = userDetails.getUser();
        Account account = user.getAccount();
        ImageFolder targetFolder = imageFolderService.getByName(account, request.getName()).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB1011,
                        "Image folder with name [" + request.getName() + "] not found!"
                )
        );

        for (Image image : targetFolder.getImages()) {
            fileStorageService.delete(
                    account,
                    FileObjectType.IMAGES,
                    targetFolder.getName(),
                    image.getFilename()
            );
            imageService.delete(image);
        }
        imageFolderService.delete(targetFolder);

        auditLogService.log(
                account.getId(),
                Optional.empty(),
                targetFolder.getId(),
                DomainType.IMAGE_FOLDER,
                ActionType.DELETE,
                targetFolder.getName(),
                userDetails.getUsername()
        );

        RootImageFolderPayload target = createRootImageFolderPayload(account);
        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        target
                )
        );
    }

    private RootImageFolderPayload createRootImageFolderPayload(final Account account) {
        List<ImageFolder> imageFolders = imageFolderService.getAllByAccount(account);
        List<Image> images = imageService.getByAccountAndImageFolderIsNull(account);

        int imageCount = 0;
        RootImageFolderPayload target = new RootImageFolderPayload();
        for (Image image : images) {
            ImagePayload targetImagePayload = ImagePayload.from(image);
            appendExternalLinkUrl(targetImagePayload);
            target.getImages().add(targetImagePayload);
            imageCount++;
        }
        for (ImageFolder imageFolder : imageFolders) {
            ImageFolderPayload targetImageFolderPayload = ImageFolderPayload.from(imageFolder);
            for (ImagePayload imagePayload : targetImageFolderPayload.getImages()) {
                appendExternalLinkUrl(imagePayload);
                imageCount++;
            }
            target.getImageFolders().add(targetImageFolderPayload);
        }
        target.setImageCount(imageCount);

        return target;
    }

    private boolean isMediaTypeSupported(String contentType) {
        for (String supportedImageType : SUPPORTED_IMAGE_TYPES) {
            if (contentType.endsWith(supportedImageType)) {
                return true;
            }
        }
        return false;
    }

    private void appendExternalLinkUrl(final ImagePayload imagePayload) {
        String url = externalLinkUrl + imagePayload.getLinkId() + "/" + imagePayload.getFilename().replaceAll(" ", "%20");
        imagePayload.setExternalUrl(url);
    }

}
