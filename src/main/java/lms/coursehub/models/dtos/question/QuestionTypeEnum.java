package lms.coursehub.models.dtos.question;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum QuestionTypeEnum {
    CHOICE("Choices Answer"),
    SHORT_ANSWER("Short Answer"),
    TRUE_FALSE("True/False");

    private final String displayName;

    QuestionTypeEnum(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static QuestionTypeEnum fromString(String value) {
        if (value == null) {
            return null;
        }

        for (QuestionTypeEnum type : QuestionTypeEnum.values()) {
            if (type.displayName.equalsIgnoreCase(value) || type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }

        throw new IllegalArgumentException(
                "Invalid QuestionTypeEnum: '" + value + "'. Expected one of: " +
                        "'" + CHOICE.displayName + "', '" + SHORT_ANSWER.displayName + "', '" + TRUE_FALSE.displayName
                        + "'");
    }
}