package lms.coursehub.helpers.mapstructs;

import lms.coursehub.models.dtos.question.*;
import lms.coursehub.models.dtos.question.QuestionResponseDto;
import lms.coursehub.models.entities.Question;
import lms.coursehub.models.entities.QuestionChoice;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface QuestionMapper {
    QuestionMapper INSTANCE = Mappers.getMapper(QuestionMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "modifier", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "usage", constant = "0L")
    @Mapping(source = "data.multiple", target = "multiple")
    @Mapping(source = "data.correctAnswer", target = "correctAnswer")
    @Mapping(source = "data.feedbackOfTrue", target = "feedbackOfTrue")
    @Mapping(source = "data.feedbackOfFalse", target = "feedbackOfFalse")
    @Mapping(target = "questionChoices", ignore = true)
    Question toEntity(CreateQuestionRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "modifier", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "usage", ignore = true)
    @Mapping(source = "data.multiple", target = "multiple")
    @Mapping(source = "data.correctAnswer", target = "correctAnswer")
    @Mapping(source = "data.feedbackOfTrue", target = "feedbackOfTrue")
    @Mapping(source = "data.feedbackOfFalse", target = "feedbackOfFalse")
    @Mapping(target = "questionChoices", ignore = true)
    Question updateEntityFromDto(UpdateQuestionRequest request, @MappingTarget Question question);

    @Mapping(source = "creator.id", target = "createdBy")
    @Mapping(source = "modifier.id", target = "modifiedBy")
    @Mapping(source = "multiple", target = "data.multiple")
    @Mapping(source = "questionChoices", target = "data.choices")
    @Mapping(source = "correctAnswer", target = "data.correctAnswer")
    @Mapping(source = "feedbackOfTrue", target = "data.feedbackOfTrue")
    @Mapping(source = "feedbackOfFalse", target = "data.feedbackOfFalse")
    @Mapping(target = "topicQuizId", ignore = true)
    QuestionResponseDto toDto(Question question);

    @Mapping(source = "text", target = "text")
    @Mapping(source = "correct", target = "correct")
    @Mapping(source = "grade", target = "grade")
    QuestionChoiceDto choiceToDto(QuestionChoice choice);
}