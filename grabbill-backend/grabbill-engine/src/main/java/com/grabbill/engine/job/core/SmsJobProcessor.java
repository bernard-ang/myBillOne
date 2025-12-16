package com.grabbill.engine.job.core;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.grabbill.core.entity.*;
import com.grabbill.core.model.DataType;
import com.grabbill.core.model.DomainType;
import com.grabbill.core.model.ProcessStatus;
import com.grabbill.core.model.SmsFieldType;
import com.grabbill.core.service.BaseActivityService;
import com.grabbill.core.service.BaseIndexRowService;
import com.grabbill.core.service.BaseRecordService;
import com.grabbill.core.service.BaseTypeService;
import com.grabbill.engine.job.JobProcessingException;
import com.grabbill.engine.service.sms.SmsResponse;
import com.grabbill.engine.service.sms.SmsResponseStatusCode;
import com.grabbill.engine.service.sms.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author michaellow
 */
@Slf4j
@Transactional
public class SmsJobProcessor extends AbstractJobProcessor {


    @Autowired
    @Qualifier("smsTypeService")
    private BaseTypeService<SmsType, SmsActivity> smsTypeService;

    @Autowired
    @Qualifier("smsActivityService")
    private BaseActivityService<SmsType, SmsActivity> smsActivityService;

    @Autowired
    @Qualifier("smsIndexRowService")
    private BaseIndexRowService<SmsType, SmsActivity, SmsIndexRow> smsIndexRowService;

    @Autowired
    @Qualifier("smsRecordService")
    private BaseRecordService<SmsRecord> smsRecordService;

    @Autowired
    private SmsService smsService;



    @Override
    void processInternal(final Job targetJob) throws JobProcessingException {
        // mark activity as processing
        SmsActivity targetActivity = smsActivityService.markAsProcessing(targetJob.getActivityId());
        SmsType targetType = targetActivity.getSmsType();
        logProcessing(targetJob, targetType.getId());

        targetActivity = smsActivityService.getById(targetJob.getActivityId()).get();
        targetType = targetActivity.getSmsType();

        processIndexRowsInternal(targetType, targetActivity);

        // mark activity as processed completely
        targetActivity.setStatus(ProcessStatus.COMPLETED);
        targetActivity.setProcessedTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
        SmsActivity updatedActivity = smsActivityService.save(targetActivity);

        // update activity statistics
        int totalSms = 0;
        int error = 0;
        int sent = 0;
        int credits = 0;

        for (SmsIndexRow indexRow : updatedActivity.getSmsIndexRows()) {

            SmsRecord record = indexRow.getSmsRecord();

            // accumulate statistical data
            totalSms++;
            if(record.isSmsStatusSent()) {
                sent++;
            } else {
                error++;
            }

            if (ProcessStatus.COMPLETED.equals(record.getStatus())) {
                credits += record.getCreditUsed();
            }
        }

        updatedActivity.setTotalSms(totalSms);
        updatedActivity.setSmsStatusSent(sent);
        updatedActivity.setSmsStatusError(error);
        updatedActivity.setSmsCreditUsed(credits);

        log.info("Activity [" + targetJob.getActivityId()
                + "] of type [" + targetJob.getDomainType()
                + "] is processed. Total credit usage is [sent=" + sent
                + ", credits=" + credits + "].");

        Integer accountId = targetJob.getAccount().getId();
        Optional<AccountUsageStatistic> accountUsageStatisticOptional = accountUsageStatisticService.getByAccountId(accountId);
        if (accountUsageStatisticOptional.isPresent()) {
            AccountUsageStatistic targetUsageStatistic = accountUsageStatisticOptional.get();
            targetUsageStatistic.setSmsCredit(targetUsageStatistic.getSmsCredit() - credits);
            targetUsageStatistic.setSmsCreditUsed((targetUsageStatistic.getSmsCreditUsed() != null ? targetUsageStatistic.getSmsCreditUsed() : 0) + credits);
            targetUsageStatistic.setTotalSmsSent((targetUsageStatistic.getTotalSmsSent() != null ? targetUsageStatistic.getTotalSmsSent() : 0) + sent);
            accountUsageStatisticService.save(targetUsageStatistic);

        } else {
            log.error("No account usage statistic found for account [" + accountId + "].");
        }

        // update type with last sent by and sent date
        SmsType updatedType = smsTypeService.getByActivity(updatedActivity);
        updatedType.setLastSentBy(updatedActivity.getLastModifiedBy());
        updatedType.setLastSentDate(updatedActivity.getLastModifiedDate());
        smsTypeService.save(updatedType);

    }

    private void processIndexRowsInternal (
            final SmsType targetType,
            final SmsActivity targetActivity
    ) throws JobProcessingException {

        Handlebars handlebars = new Handlebars();
        for (SmsIndexRow indexRow : targetActivity.getSmsIndexRows()) {
            String mobileNo = SmsFieldType.INDEX_FIELD.equals(targetType.getSmsFieldType()) ?
                    indexRow.getText1() : indexRow.getText2();

            SmsRecord record = indexRow.getSmsRecord();
            // IMPORTANT: skipping those index row with record (already processed)
            if (record == null) {
                record = new SmsRecord();
                record.setName(mobileNo);
                record.setPriority(targetActivity.getPriority());
                record.setSmsActivity(targetActivity);
                record.setSmsIndexRow(indexRow);

                try {
                    Map<String, String> parameterMap = buildParameterMap(indexRow, targetActivity.getSmsActivityIndexFields());
                    Template smsTemplate = handlebars.compileInline(targetActivity.getSmsContent());
                    String processedSmsContent = smsTemplate.apply(parameterMap);
                    record.setSmsContent(processedSmsContent);

                    SmsResponse smsResponse = smsService.send(targetActivity.getSmsFrom(), mobileNo, processedSmsContent);
                    record.setSmsStatusCode(smsResponse.getStatusCode().getCode());
                    if (SmsResponseStatusCode.OK.equals(smsResponse.getStatusCode())) {
                        record.setSmsStatusSent(true);
                        record.setCreditUsed(smsResponse.getSmsSplit());
                        record.setStatus(ProcessStatus.COMPLETED);
                        record.setMessage(smsResponse.getRaw());

                    } else {
                        record.setSmsStatusSent(false);
                        record.setStatus(ProcessStatus.ERROR);
                        record.setMessage(smsResponse.getRaw());
                        record.setSmsErrorMessage(smsResponse.getStatusCode().getDescription());
                    }

                } catch (Exception e) {
                    log.error("Unknown error while processing index row", e);
                    record.setSmsStatusSent(false);
                    record.setStatus(ProcessStatus.ERROR);
                    record.setMessage((e.getMessage().length() < 255) ?
                            e.getMessage() : e.getMessage().substring(0, 255));
                }

                // done processing record
                record.setProcessedTimestamp(OffsetDateTime.now(ZoneOffset.UTC));

                // link record to index row
                SmsRecord savedRecord = smsRecordService.save(record);
                indexRow.setSmsType(targetType);
                indexRow.setSmsRecord(savedRecord);
                smsIndexRowService.save(indexRow);
            }
        }
    }

    private Map<String, String> buildParameterMap(
            final SmsIndexRow indexRow,
            final List<SmsActivityIndexField> indexFields
    ) {
        Map<String, String> parameterMap = new HashMap<>();
        for (int i = 0; i < indexFields.size(); i++) {
            SmsActivityIndexField indexField = indexFields.get(i);
            if (DataType.TEXT.equals(indexField.getDataType()) || DataType.EMAIL.equals(indexField.getDataType())) {
                String text = indexRowHelper.getText(i + 1, indexRow);
                parameterMap.put(indexField.getName(), text != null ? text : "");

            } else if (DataType.NUMBER.equals(indexField.getDataType())) {
                Integer number = indexRowHelper.getNumber(i + 1, indexRow);
                parameterMap.put(indexField.getName(), number != null ? String.valueOf(number) : "");

            } else if (DataType.DATE.equals(indexField.getDataType())) {
                LocalDate date = indexRowHelper.getDate(i + 1, indexRow);
                parameterMap.put(indexField.getName(), date != null ? DATE_TIME_FORMATTER.format(date) : "");
            }
        }

        return parameterMap;
    }

    @Override
    void purgeInternal(final Job targetJob) throws JobProcessingException {
        throw new UnsupportedOperationException("Purge not supported for SMS Job");
    }

    @Override
    void startPrepareInternal(final Job job) throws JobProcessingException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    void endPrepareInternal(final Job job) throws JobProcessingException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    BaseActivity getBaseActivity(final long id) {
        return smsActivityService.getById(id).get();
    }

    @Override
    public DomainType getSupportedActivityType() {
        return DomainType.SMS;
    }

}
