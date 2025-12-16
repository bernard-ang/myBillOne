package com.grabbill.core.service;

import com.grabbill.core.entity.*;
import com.grabbill.core.exception.GrabbillException;
import com.grabbill.core.exception.GrabbillSftpException;
import com.grabbill.core.model.ProcessStatus;
import com.grabbill.core.model.sftp.ExcelIndexRow;
import com.grabbill.core.model.sftp.ExcelIndexRowException;
import com.grabbill.core.model.sftp.ExcelIndexRows;
import com.grabbill.core.model.sftp.SftpFolderValidationSummary;
import com.grabbill.core.model.storage.FileObjectType;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.StringUtils;

import javax.transaction.Transactional;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @author michaellow
 */
@Slf4j
@Transactional
public class MTWhatsappActivitySftpServiceImpl
        implements BaseActivitySftpService<MTWhatsAppType, MTWhatsAppActivity, MTWhatsAppIndexField> {

    private static final int FILENAME_COLUMN_INDEX = 2;

    @Autowired
    private AccountUsageStatisticService accountUsageStatisticService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    @Qualifier("mtWhatsAppFileService")
    private BaseFileService<MTWhatsAppType, MTWhatsAppActivity, MTWhatsAppFile> mtWhatsAppFileService;

    @Autowired
    private SftpService sftpService;


    @Override
    public SftpFolderValidationSummary validate(
            final Account account,
            final MTWhatsAppType type,
            final List<MTWhatsAppIndexField> indexFields,
            final MTWhatsAppActivity activity
    ) throws GrabbillException {
        if (!activity.isSftp()) {
            throw new GrabbillException("Activity does not support data input from sftp folder");
        }

        List<String> files = new ArrayList<>();
        ExcelIndexRows excelIndexRows = null;
        String indexRowSourceFile = null;

        try {
            ChannelSftp channelSftp = sftpService.connect(account);
            sftpService.changeDirectory(channelSftp, activity.getSftpPath());
            for (ChannelSftp.LsEntry entry : channelSftp.ls("./" + IN_FOLDER)) {
                if (!entry.getAttrs().isDir() && !entry.getFilename().startsWith(".")) {
                    if (entry.getFilename().endsWith(PDF_EXT)) {
                        files.add(entry.getFilename());

                    } else if (entry.getFilename().endsWith(EXCEL_EXT)) {
                        InputStream excelInputStream = channelSftp.get(channelSftp.pwd() + "/" + IN_FOLDER + "/" + entry.getFilename());
                        try {
                            excelIndexRows = ExcelIndexRows.fromExcel(excelInputStream, indexFields, type.getCode());
                        } catch (ExcelIndexRowException e) {
                            throw new GrabbillSftpException(e.getMessage(), e);
                        }
                        if (!StringUtils.hasLength(indexRowSourceFile)) {
                            indexRowSourceFile = entry.getFilename();
                        } else {
                            throw new GrabbillSftpException("Unable to process " + entry.getFilename() +
                                    " as more than 1 input file detected (existing " + indexRowSourceFile + ").");
                        }

                    } else if (entry.getFilename().endsWith(CSV_EXT)) {
                        InputStream csvInputStream = channelSftp.get(channelSftp.pwd() + "/" + IN_FOLDER + "/" + entry.getFilename());
                        try {
                            excelIndexRows = ExcelIndexRows.fromCsv(csvInputStream, indexFields, type.getCsvSeparator(), type.getCode());
                        } catch (ExcelIndexRowException e) {
                            throw new GrabbillSftpException(e.getMessage(), e);
                        }
                        if (!StringUtils.hasLength(indexRowSourceFile)) {
                            indexRowSourceFile = entry.getFilename();
                        } else {
                            throw new GrabbillSftpException("Unable to process " + entry.getFilename() +
                                    " as more than 1 input file detected (existing " + indexRowSourceFile + ").");
                        }
                    }
                }
            }
            sftpService.disconnect(channelSftp);

        } catch (GrabbillSftpException | SftpException e) {
            log.warn(e.getMessage(), e);
            SftpFolderValidationSummary summary = new SftpFolderValidationSummary();
            summary.getGenericErrors().add(e.getMessage());
            return summary;
        }

        SftpFolderValidationSummary summary = SftpFolderValidationSummary.from(
                type.isHasAttachment(),
                files,
                excelIndexRows,
                FILENAME_COLUMN_INDEX
        );
        if (summary.hasError()) {
            activity.setStatus(ProcessStatus.DRAFT);
        }
        return summary;
    }

    @Override
    public void process(
            final Account account,
            final MTWhatsAppType type,
            final List<MTWhatsAppIndexField> indexFields,
            final MTWhatsAppActivity activity
    ) throws GrabbillException {
        if (!activity.isSftp()) {
            throw new GrabbillException("Activity does not support data input from sftp folder");
        }

        ExcelIndexRows excelIndexRows = null;
        String indexRowSourceFile = null;

        try {
            ChannelSftp channelSftp = sftpService.connect(account);
            sftpService.changeDirectory(channelSftp, activity.getSftpPath());

            // build arc && err folder
            boolean hasArc = false;
            boolean hasErr = false;
            for (ChannelSftp.LsEntry entry : channelSftp.ls("./")) {
                if (entry.getAttrs().isDir()) {
                    if (entry.getFilename().equals(ARCHIVE_FOLDER)) {
                        hasArc = true;
                    } else if (entry.getFilename().equals(ERROR_FOLDER)) {
                        hasErr = true;
                    }
                }
            }
            if (!hasArc) {
                channelSftp.mkdir(channelSftp.pwd() + "/" + ARCHIVE_FOLDER);
                channelSftp.chmod(511, channelSftp.pwd() + "/" + ARCHIVE_FOLDER);
            }
            if (!hasErr) {
                channelSftp.mkdir(channelSftp.pwd() + "/" + ERROR_FOLDER);
                channelSftp.chmod(511, channelSftp.pwd() + "/" + ERROR_FOLDER);
            }

            long totalFileSize = 0;
            for (ChannelSftp.LsEntry entry : channelSftp.ls("./" + IN_FOLDER)) {

                if (!entry.getAttrs().isDir()) {
                    InputStream inputStream = channelSftp.get(channelSftp.pwd() + "/" + IN_FOLDER + "/" + entry.getFilename());

                    if (entry.getFilename().endsWith(PDF_EXT)) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        int nRead;
                        byte[] data = new byte[4];
                        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                            baos.write(data, 0, nRead);
                        }
                        baos.flush();
                        byte[] bytes = baos.toByteArray();
                        baos.close();

                        fileStorageService.upload(
                                account,
                                FileObjectType.WHATSAPP,
                                activity.getId().toString(),
                                entry.getFilename(),
                                bytes
                        );
                        Item item = fileStorageService.find(
                                account,
                                FileObjectType.WHATSAPP,
                                activity.getId().toString(),
                                entry.getFilename()
                        );

                        Optional<MTWhatsAppFile> fileOptional = mtWhatsAppFileService.getByNameAndActivity(
                                entry.getFilename(), activity);
                        if (fileOptional.isPresent()) {
                            MTWhatsAppFile fileInstance = fileOptional.get();
                            totalFileSize -= fileInstance.getFileSize();
                            fileInstance.setName(entry.getFilename());
                            fileInstance.setFileType("application/pdf");
                            fileInstance.setFileSize(item.size());
                            fileInstance.setMtWhatsAppType(type);
                            fileInstance.setMtWhatsAppActivity(activity);
                            mtWhatsAppFileService.save(fileInstance);

                        } else {
                            MTWhatsAppFile newFileInstance = new MTWhatsAppFile();
                            newFileInstance.setName(entry.getFilename());
                            newFileInstance.setFileType("application/pdf");
                            newFileInstance.setFileSize(item.size());
                            newFileInstance.setMtWhatsAppType(type);
                            newFileInstance.setMtWhatsAppActivity(activity);
                            mtWhatsAppFileService.save(newFileInstance);
                        }
                        totalFileSize += item.size();
                        inputStream.close();

                        channelSftp.rename(
                                channelSftp.pwd() + "/" + IN_FOLDER + "/" + entry.getFilename(),
                                channelSftp.pwd() + "/" + ARCHIVE_FOLDER + "/" + entry.getFilename()
                        );

                    } else if (entry.getFilename().endsWith(EXCEL_EXT)) {
                        try {
                            excelIndexRows = ExcelIndexRows.fromExcel(inputStream, indexFields, type.getCode());
                        } catch (ExcelIndexRowException e) {
                            throw new GrabbillSftpException(e.getMessage(), e);
                        }
                        if (!StringUtils.hasLength(indexRowSourceFile)) {
                            indexRowSourceFile = entry.getFilename();
                        } else {
                            throw new GrabbillSftpException("Unable to process " + entry.getFilename() +
                                    " as more than 1 input file detected (existing " + indexRowSourceFile + ").");
                        }

                        inputStream.close();
                        channelSftp.rename(
                                channelSftp.pwd() + "/" + IN_FOLDER + "/" + entry.getFilename(),
                                channelSftp.pwd() + "/" + ARCHIVE_FOLDER + "/" + entry.getFilename()
                        );

                    }  else if (entry.getFilename().endsWith(CSV_EXT)) {
                        try {
                            excelIndexRows = ExcelIndexRows.fromCsv(inputStream, indexFields, type.getCsvSeparator(), type.getCode());
                        } catch (ExcelIndexRowException e) {
                            throw new GrabbillSftpException(e.getMessage(), e);
                        }
                        if (!StringUtils.hasLength(indexRowSourceFile)) {
                            indexRowSourceFile = entry.getFilename();
                        } else {
                            throw new GrabbillSftpException("Unable to process " + entry.getFilename() +
                                    " as more than 1 input file detected (existing " + indexRowSourceFile + ").");
                        }

                        inputStream.close();
                        channelSftp.rename(
                                channelSftp.pwd() + "/" + IN_FOLDER + "/" + entry.getFilename(),
                                channelSftp.pwd() + "/" + ARCHIVE_FOLDER + "/" + entry.getFilename()
                        );

                    } else {
                        channelSftp.rename(
                                channelSftp.pwd() + "/" + IN_FOLDER + "/" + entry.getFilename(),
                                channelSftp.pwd() + "/" + ERROR_FOLDER + "/" + entry.getFilename()
                        );
                    }
                }

            }

            List<MTWhatsAppIndexRow> targetIndexRows = activity.getMtWhatsAppIndexRows();
            List<ExcelIndexRow> excelIndexRowList = excelIndexRows.getRows();
            int i = 0;
            for (; i < excelIndexRowList.size(); i++) {
                if (targetIndexRows.size() > i) {
                    excelIndexRowList.get(i).to(targetIndexRows.get(i));

                } else {
                    MTWhatsAppIndexRow indexRow = new MTWhatsAppIndexRow();
                    indexRow.setMtWhatsAppActivity(activity);
                    indexRow.setMtWhatsAppType(activity.getMtWhatsAppType());
                    excelIndexRowList.get(i).to(indexRow);
                    activity.getMtWhatsAppIndexRows().add(indexRow);
                }
            }

            int currentSize = targetIndexRows.size();
            int newSize = excelIndexRowList.size();
            for(; i < currentSize; i++) {
                activity.getMtWhatsAppIndexRows().remove(newSize);
            }

            AccountUsageStatistic targetUsageStatistic = accountUsageStatisticService.getByAccountId(account.getId()).get();
            targetUsageStatistic.setTotalStorageUsed(targetUsageStatistic.getTotalStorageUsed() + totalFileSize);
            accountUsageStatisticService.save(targetUsageStatistic);

            sftpService.disconnect(channelSftp);

        } catch (SftpException | IOException e) {
            throw new GrabbillException("Failed to process sftp folder!", e);
        }
    }

}
