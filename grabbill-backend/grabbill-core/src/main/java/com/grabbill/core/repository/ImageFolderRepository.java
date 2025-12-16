package com.grabbill.core.repository;

import com.grabbill.core.entity.Account;
import com.grabbill.core.entity.ImageFolder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


/**
 * @author michaellow
 */
public interface ImageFolderRepository extends JpaRepository<ImageFolder, Long> {

    List<ImageFolder> findAllByAccountIs(Account account);

    Optional<ImageFolder> findByAccountIsAndNameIs(Account account, String folderName);

}
