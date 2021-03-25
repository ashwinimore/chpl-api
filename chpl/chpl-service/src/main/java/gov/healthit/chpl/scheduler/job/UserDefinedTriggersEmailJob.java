package gov.healthit.chpl.scheduler.job;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.mail.MessagingException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.domain.schedule.ChplRepeatableTrigger;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.SchedulerManager;
import gov.healthit.chpl.util.EmailBuilder;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "userDefinedTriggersEmailJobLogger")
public class UserDefinedTriggersEmailJob extends QuartzJob {
    private String[] standardJobDataKeys = {"acb", "acbSpecific", "email", "authorities", "frequency"};

    @Autowired
    private SchedulerManager schedulerManager;

    @Autowired
    private CertificationBodyDAO acbDao;

    @Autowired
    private Environment env;

    public UserDefinedTriggersEmailJob() throws Exception {
        super();
    }

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the User-Defined Triggers Email job. *********");
        setSecurityContext();
        try {
            List<ChplRepeatableTrigger> userTriggers = schedulerManager.getAllTriggersForUser();
            List<List<String>> csvRows = formatAsCsv(userTriggers);
            sendEmail(jobContext, csvRows);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        LOGGER.info("********* Completed the User-Defined Triggers Email job. *********");
    }

    private List<List<String>> formatAsCsv(List<ChplRepeatableTrigger> userTriggers) {
        List<List<String>> triggerCsv = new ArrayList<List<String>>();
        userTriggers.stream().forEach(trigger -> {
            triggerCsv.add(getTriggerCsv(trigger));
        });
        return triggerCsv;
    }

    private List<String> getTriggerCsv(ChplRepeatableTrigger userTrigger) {
        List<String> triggerCsv = new ArrayList<String>();
        triggerCsv.add(userTrigger.getJob().getName());
        triggerCsv.add(userTrigger.getEmail());
        triggerCsv.add(getAcbs(userTrigger));
        triggerCsv.add(userTrigger.getCronSchedule());
        triggerCsv.add(getCronDescription(userTrigger.getCronSchedule()));
        JobDataMap jobDataMap = userTrigger.getJob().getJobDataMap();
        if (jobDataMap != null && jobDataMap.getKeys() != null && jobDataMap.getKeys().length > 0) {
            triggerCsv.add(Stream.of(jobDataMap.getKeys())
                .filter(key -> key != null && jobDataMap.get(key) != null)
                .filter(key -> !Arrays.stream(standardJobDataKeys).anyMatch(key::equalsIgnoreCase))
                .map(key -> key + ":" + jobDataMap.get(key).toString())
                .collect(Collectors.joining(";")));
        } else {
            triggerCsv.add("");
        }
        return triggerCsv;
    }

    private String getAcbs(ChplRepeatableTrigger trigger) {
        if (trigger == null || trigger.getAcb() == null) {
            return "";
        }
        //expect a comma-separated list of ACB IDs
        String acbs = trigger.getAcb();
        String[] acbList = acbs.split(",");
        if (acbList == null || acbList.length == 0) {
            return "";
        }
        return Stream.of(acbList)
            .map(acbIdStr -> new Long(acbIdStr))
            .map(acbId -> {
                try {
                    return acbDao.getById(acbId);
                } catch (EntityRetrievalException e) {
                    LOGGER.error("Could not find ACB with ID " + acbId, e);
                }
                return null;
            })
            .filter(acb -> acb != null)
            .map(acb -> acb.getName())
            .collect(Collectors.joining(";"));
    }

    private String getCronDescription(String quartzCronString) {
        if (StringUtils.isEmpty(quartzCronString)) {
            return "";
        }

        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);
        CronParser parser = new CronParser(cronDefinition);
        Cron quartzCron = parser.parse(quartzCronString);
        CronDescriptor descriptor = CronDescriptor.instance(Locale.US);
        return descriptor.describe(quartzCron);
    }

    private List<String> getHeaderRow() {
        List<String> row = new ArrayList<String>();
        row.add("Job Name");
        row.add("Email Address");
        row.add("ACB(s)");
        row.add("Quartz Cron Schedule");
        row.add("Cron Schedule Description");
        row.add("Extra Job Data");
        return row;
    }

    private List<String> getFooterRow() {
        List<String> row = new ArrayList<String>();
        row.add("");
        row.add("");
        row.add("");
        row.add(env.getProperty("userTriggersReport.cronDescriptionUrl"));
        row.add("");
        row.add("");
        return row;
    }

    private void sendEmail(JobExecutionContext jobContext, List<List<String>> csvRows)
            throws MessagingException {
        LOGGER.info("Sending email to {} with contents {} and a total of {} user triggers",
                getEmailRecipients(jobContext).get(0), getHtmlMessage(csvRows.size()));

        EmailBuilder emailBuilder = new EmailBuilder(env);
        emailBuilder.recipients(getEmailRecipients(jobContext))
        .subject(getSubject(jobContext))
        .htmlMessage(getHtmlMessage(csvRows.size()))
        .fileAttachments(getAttachments(csvRows))
        .acbAtlHtmlFooter()
        .sendEmail();
    }

    private String getSubject(JobExecutionContext jobContext) {
        return env.getProperty("userTriggersReport.subject");
    }

    private List<File> getAttachments(List<List<String>> csvRows) {
        List<File> attachments = new ArrayList<File>();
        File csvFile = getCsvFile(csvRows);
        if (csvFile != null) {
            attachments.add(csvFile);
        }
        return attachments;
    }

    private File getCsvFile(List<List<String>> csvRows) {
        File csvFile = null;
        if (csvRows.size() > 0) {
            String filename = env.getProperty("userTriggersReport.filename");
            if (csvRows.size() > 0) {
                csvFile = getOutputFile(csvRows, filename);
            }
        }
        return csvFile;
    }

    private File getOutputFile(List<List<String>> rows, String reportFilename) {
        File temp = null;
        try {
            temp = File.createTempFile(reportFilename, ".csv");
            temp.deleteOnExit();

            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(temp),
                    Charset.forName("UTF-8").newEncoder());
                    CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL)) {
                writer.write('\ufeff');
                csvPrinter.printRecord(getHeaderRow());
                for (List<String> rowValue : rows) {
                    csvPrinter.printRecord(rowValue);
                }
                csvPrinter.printRecord(getFooterRow());
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return temp;
    }

    private String getHtmlMessage(Integer rowCount) {
        return String.format(env.getProperty("userTriggersReport.htmlBody"), rowCount);
    }

    private List<String> getEmailRecipients(JobExecutionContext jobContext) {
        return Arrays.asList(jobContext.getMergedJobDataMap().getString("email"));
    }

    private void setSecurityContext() {
        JWTAuthenticatedUser adminUser = new JWTAuthenticatedUser();
        adminUser.setFullName("Administrator");
        adminUser.setId(-2L);
        adminUser.setFriendlyName("Admin");
        adminUser.setSubjectName("admin");
        adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
        SecurityContextHolder.getContext().setAuthentication(adminUser);
    }
}
