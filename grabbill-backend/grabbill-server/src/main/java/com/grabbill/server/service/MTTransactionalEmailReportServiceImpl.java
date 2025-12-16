package com.grabbill.server.service;

import com.grabbill.core.entity.*;
import com.grabbill.core.model.DomainType;
import com.grabbill.core.model.ReportType;
import com.grabbill.core.service.EmbeddedLinkClickService;
import com.grabbill.core.service.EmbeddedLinkService;
import com.grabbill.core.service.UnsubscribedEmailService;
import com.grabbill.server.dto.report.ActivityRecordRow;
import com.grabbill.server.dto.report.ActivitySummaryRow;
import com.grabbill.server.dto.report.UrlClickByEmailRow;
import com.grabbill.server.dto.report.UrlClickSummaryRow;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * @author michaellow
 */
public class MTTransactionalEmailReportServiceImpl extends BaseReportService implements MTTransactionalEmailReportService {

    @Autowired
    private EmbeddedLinkService embeddedLinkService;

    @Autowired
    private EmbeddedLinkClickService embeddedLinkClickService;

    @Autowired
    private UnsubscribedEmailService unsubscribedEmailService;


    @Override
    public Workbook generateMtTransactionalEmailReport(
            final List<MTTransactionalEmailActivity> activities,
            final List<ReportType> reportTypes,
            final ZoneId zoneId,
            final Boolean encryptAttachmentPassword,
            final boolean hasWhatsApp
    ) {
        Workbook workbook = new XSSFWorkbook();
        for (ReportType reportType : reportTypes) {
            if (ReportType.ACTIVITY_SUMMARY.equals(reportType)) {
                List<ActivitySummaryRow> activitySummaryRows = new ArrayList<>();
                for (MTTransactionalEmailActivity activity : activities) {
                    MTTransactionalEmailType type = activity.getMtTransactionalEmailType();

                    ActivitySummaryRow activitySummaryRow = new ActivitySummaryRow();
                    activitySummaryRow.setTypeId(type.getId());
                    activitySummaryRow.setTypeName(type.getName());
                    activitySummaryRow.setActivityId(activity.getId());
                    activitySummaryRow.setActivityName(activity.getName());
                    activitySummaryRow.setStartAt(ZonedDateTime.ofInstant(activity.getSubmittedTimestamp().toInstant(), zoneId));
                    activitySummaryRow.setCompletedAt(ZonedDateTime.ofInstant(activity.getProcessedTimestamp().toInstant(), zoneId));
                    activitySummaryRow.setTotalEmails(activity.getMtTransactionalEmailIndexRows().size());
                    activitySummaryRow.setTotalSent(activity.getEmailStatusSent());
                    activitySummaryRow.setTotalSkip(
                            activity.getEmailStatusBouncedSkip() == null ? 0 : activity.getEmailStatusBouncedSkip() +
                                    activity.getEmailStatusUnsubscribedSkip());
                    activitySummaryRow.setTotalUnsubscribed(unsubscribedEmailService.totalUnsubscribedEmailByActivity(
                            type.getAccount().getId(),
                            DomainType.TRANSACTIONAL_EMAIL,
                            type.getId(),
                            activity.getId()));
                    activitySummaryRow.setTotalBounced(activity.getEmailStatusBounced());
                    activitySummaryRow.setTotalOpened(activity.getEmailStatusOpened());

                    activitySummaryRow.setTotalWhatsAppSent(activity.getWhatsAppStatusSent() == null ? 0: activity.getWhatsAppStatusSent());
                    activitySummaryRow.setTotalWhatsAppSkip(activity.getWhatsAppStatusSkip() == null ? 0: activity.getWhatsAppStatusSkip());
                    activitySummaryRow.setTotalWhatsAppRead(activity.getWhatsAppStatusRead() == null ? 0: activity.getWhatsAppStatusRead());
                    activitySummaryRow.setTotalWhatsAppDelivered(activity.getWhatsAppStatusDelivered() == null ? 0: activity.getWhatsAppStatusDelivered());
                    activitySummaryRow.setTotalWhatsAppAcknowledge(activity.getWhatsAppStatusAcknowledge() == null ? 0: activity.getWhatsAppStatusAcknowledge());
                    activitySummaryRow.setTotalWhatsAppFailed(activity.getWhatsAppStatusFailed() == null ? 0: activity.getWhatsAppStatusFailed());

                    activitySummaryRows.add(activitySummaryRow);
                }
                buildActivitySummarySheet(workbook, activitySummaryRows, hasWhatsApp);

            } else if (ReportType.ACTIVITY_RECORDS.equals(reportType)) {
                List<List<ActivityRecordRow>> allRecordRows = new ArrayList<>();
                Set<String> dynamicHeaders = new LinkedHashSet<>();
                for (MTTransactionalEmailActivity activity : activities) {
                    MTTransactionalEmailType type = activity.getMtTransactionalEmailType();
                    List<MTTransactionalEmailIndexField> indexFields = type.getMtTransactionalEmailIndexFields();

                    List<ActivityRecordRow> activityRecordRows = new ArrayList<>();
                    for (MTTransactionalEmailRecord record : activity.getMtTransactionalEmailRecords()) {
                        MTTransactionalEmailIndexRow indexRow = record.getMtTransactionalEmailIndexRow();

                        ActivityRecordRow activityRecordRow = new ActivityRecordRow();
                        activityRecordRow.setRecordId(record.getId());
                        activityRecordRow.setTypeId(type.getId());
                        activityRecordRow.setTypeName(type.getName());
                        activityRecordRow.setActivityId(activity.getId());
                        activityRecordRow.setActivityName(activity.getName());
                        activityRecordRow.setEmail(record.getName());
                        activityRecordRow.setProcessStatus(record.getStatus());
                        activityRecordRow.setMessage(record.getMessage());
                        activityRecordRow.setEmailStatusSent(record.isEmailStatusSent());
                        activityRecordRow.setEmailStatusSkipUnsubscribed(record.isEmailStatusSkipUnsubscribed());
                        activityRecordRow.setEmailStatusSkipBounced(record.isEmailStatusSkipBounced());
                        activityRecordRow.setEmailStatusSoftBounce(record.isEmailStatusSoftBounce());
                        activityRecordRow.setEmailStatusHardBounce(record.isEmailStatusHardBounce());
                        activityRecordRow.setEmailDsnReceivedConfirmation(record.isEmailDsnReceivedConfirmation());
                        activityRecordRow.setEmailDsnMessage(record.getEmailDsnMessage());

                        activityRecordRow.setWhatsAppTemplateBody(record.getWhatsappBodyContent());
                        activityRecordRow.setWhatsAppStatusSent(record.getWhatsappStatusSent() != null && record.getWhatsappStatusSent());
                        activityRecordRow.setWhatsAppStatusSkip(record.getWhatsappStatusSkip() != null && record.getWhatsappStatusSkip());
                        activityRecordRow.setWhatsAppStatusDelivered(record.getWhatsappStatusDelivered() != null && record.getWhatsappStatusDelivered());
                        activityRecordRow.setWhatsAppStatusRead(record.getWhatsappStatusRead() != null && record.getWhatsappStatusRead());
                        activityRecordRow.setWhatsAppStatusAcknowledge(record.getWhatsappStatusAcknowledge() != null && record.getWhatsappStatusAcknowledge());
                        activityRecordRow.setWhatsAppStatusFailed(record.getWhatsAppStatusFailed() != null && record.getWhatsAppStatusFailed());
                        activityRecordRow.setWhatsAppStatusFailedMessage(record.getWhatsAppStatusFailedMessage());

                        for (int i = 0; i < indexFields.size(); i++) {
                            MTTransactionalEmailIndexField indexField = indexFields.get(i);
                            if (indexField.isApplicable()) {
                                if (indexField.getLabel().equals("Attachment Password")) {
                                    activityRecordRow.getDynamicFields().put(indexField.getLabel(), encryptAttachmentPassword ? "***" : getValue(i, indexRow, indexField.getDataType()));
                                    dynamicHeaders.add(indexField.getLabel());
                                } else {
                                    activityRecordRow.getDynamicFields().put(indexField.getLabel(), getValue(i, indexRow, indexField.getDataType()));
                                    dynamicHeaders.add(indexField.getLabel());
                                }
                            }
                        }

                        activityRecordRows.add(activityRecordRow);
                    }
                    allRecordRows.add(activityRecordRows);
                }

                buildActivityRecordsSheet(workbook, dynamicHeaders, allRecordRows, hasWhatsApp);

            } else if (ReportType.URL_CLICK_SUMMARY.equals(reportType)) {
                List<UrlClickSummaryRow> urlClickSummaryRows = new ArrayList<>();
                for (MTTransactionalEmailActivity activity : activities) {

                    MTTransactionalEmailType type = activity.getMtTransactionalEmailType();
                    for (EmbeddedLink embeddedLink : embeddedLinkService.getByDomainTypeAndActivityIdIs(DomainType.TRANSACTIONAL_EMAIL, activity.getId())) {
                        UrlClickSummaryRow urlClickSummaryRow = new UrlClickSummaryRow();
                        urlClickSummaryRow.setTypeId(type.getId());
                        urlClickSummaryRow.setTypeName(type.getName());
                        urlClickSummaryRow.setActivityId(activity.getId());
                        urlClickSummaryRow.setActivityName(activity.getName());
                        urlClickSummaryRow.setUrl(embeddedLink.getUrl());
                        urlClickSummaryRow.setUniqueClickCount(embeddedLinkClickService.countUniqueClicksByEmbeddedLinkId(embeddedLink.getId()));
                        urlClickSummaryRow.setClickCount(embeddedLinkClickService.countTotalClicksByEmbeddedLinkId(embeddedLink.getId()));

                        urlClickSummaryRows.add(urlClickSummaryRow);
                    }
                }

                buildUrlClickSummarySheet(workbook, urlClickSummaryRows);

            } else if (ReportType.URL_CLICK_RECORDS.equals(reportType)) {
                List<UrlClickByEmailRow> urlClickByEmailRows = new ArrayList<>();
                for (MTTransactionalEmailActivity activity : activities) {

                    MTTransactionalEmailType type = activity.getMtTransactionalEmailType();
                    for (EmbeddedLink embeddedLink : embeddedLinkService.getByDomainTypeAndActivityIdIs(DomainType.TRANSACTIONAL_EMAIL, activity.getId())) {

                        Map<String, List<EmbeddedLinkClick>> emailToEmbeddedLinkClickMap = new HashMap<>();
                        for (EmbeddedLinkClick embeddedLinkClick : embeddedLinkClickService.getByEmbeddedLinkId(embeddedLink.getId())) {
                            if (!emailToEmbeddedLinkClickMap.containsKey(embeddedLinkClick.getEmail())) {
                                emailToEmbeddedLinkClickMap.put(embeddedLinkClick.getEmail(), new ArrayList<>());
                            }

                            emailToEmbeddedLinkClickMap.get(embeddedLinkClick.getEmail()).add(embeddedLinkClick);
                        }

                        for (String email : emailToEmbeddedLinkClickMap.keySet()) {
                            UrlClickByEmailRow urlClickByEmailRow = new UrlClickByEmailRow();
                            urlClickByEmailRow.setTypeId(type.getId());
                            urlClickByEmailRow.setTypeName(type.getName());
                            urlClickByEmailRow.setActivityId(activity.getId());
                            urlClickByEmailRow.setActivityName(activity.getName());
                            urlClickByEmailRow.setUrl(embeddedLink.getUrl());

                            List<EmbeddedLinkClick> embeddedLinkClicks = emailToEmbeddedLinkClickMap.get(email);
                            EmbeddedLinkClick embeddedLinkClick = embeddedLinkClicks.get(0);
                            urlClickByEmailRow.setClickedAt(ZonedDateTime.ofInstant(embeddedLinkClick.getClickedDate().toInstant(), zoneId));
                            urlClickByEmailRow.setEmail(embeddedLinkClick.getEmail());
                            urlClickByEmailRow.setClickCount(embeddedLinkClicks.size());

                            urlClickByEmailRows.add(urlClickByEmailRow);
                        }
                    }
                }

                buildUrlClickByEmailSheet(workbook, urlClickByEmailRows);
            }
        }

        return workbook;
    }

}
