package gov.healthit.chpl.questionableactivity.domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.util.DateUtil;
import gov.healthit.chpl.util.EasternToSystemLocalDateTimeDeserializer;
import gov.healthit.chpl.util.SystemToEasternLocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class QuestionableActivity implements Serializable {
    private static final long serialVersionUID = -8153861360218726537L;
    public static final List<String> CSV_HEADINGS = Stream.of("ONC-ACB", "Developer", "Product", "Version",
            "CHPL Product Number", "Current Certification Status", "Link", "Activity Timestamp", "Responsible User",
            "Activity Level", "Activity Type", "Activity", "Reason for Status Change", "Reason").toList();

    private String triggerLevel;
    private String triggerName;
    private Long activityId;
    private String before;
    private String after;
    @JsonDeserialize(using = EasternToSystemLocalDateTimeDeserializer.class)
    @JsonSerialize(using = SystemToEasternLocalDateTimeSerializer.class)
    private LocalDateTime activityDate;
    private Long userId;
    private String username;
    private String certificationStatusChangeReason;
    private String reason;
    private Long developerId;
    private String developerName;
    private Long productId;
    private String productName;
    private Long versionId;
    private String versionName;
    private Long listingId;
    private String chplProductNumber;
    private Long acbId;
    private String acbName;
    private Long certificationStatusId;
    private String certificationStatusName;
    private Long certificationCriterionId;

    public List<String> toListOfStringsForCsv() {
        List<String> csvFields = new ArrayList<String>();
        csvFields.add(acbName);
        csvFields.add(developerName);
        csvFields.add(productName);
        csvFields.add(versionName);
        csvFields.add(chplProductNumber);
        csvFields.add(certificationStatusName);
        csvFields.add(DateUtil.formatInEasternTime(activityDate));
        //TODO: get url begin in here
        csvFields.add("#/reports/listings/" + listingId);
        csvFields.add(username);
        csvFields.add(triggerLevel);
        csvFields.add(triggerName);
        if (StringUtils.isEmpty(before)) {
            csvFields.add("Added " + after);
        } else if (StringUtils.isEmpty(after)) {
            csvFields.add("Removed " + before);
        } else {
            csvFields.add("From " + before + " to " + after);
        }
        csvFields.add(certificationStatusChangeReason);
        csvFields.add(reason);
        return csvFields;
    }
}
