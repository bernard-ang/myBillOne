package com.grabbill.core.repository;

import com.grabbill.core.entity.Account;
import com.grabbill.core.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * @author michaellow
 */
public interface ImageRepository extends JpaRepository<Image, Long> {

    List<Image> findAllByAccountIs(Account account);

    Optional<Image> findByAccountIsAndIdIs(Account account, Long id);

    List<Image> findByAccountIsAndImageFolder_Name(Account account, String imageFolderName);

    List<Image> findByAccountIsAndImageFolderIsNull(Account account);

    List<Image> findByAccountIsAndFilenameIsAndImageFolder_Name(Account account, String filename, String imageFolderName);

    List<Image> findByAccountIsAndFilenameIsAndImageFolderIsNull(Account account, String filename);

    Optional<Image> findByLinkIdIsAndFilenameIs(String linkId, String fileName);

}
