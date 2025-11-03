package lms.coursehub.helpers.mapstructs;

import lms.coursehub.models.dtos.quiz.QuizResponseAnswerDto;
import lms.coursehub.models.dtos.quiz.QuizResponseDto;
import lms.coursehub.models.entities.QuizResponse;
import lms.coursehub.models.entities.QuizResponseAnswer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface QuizResponseMapper {
    QuizResponseMapper INSTANCE = Mappers.getMapper(QuizResponseMapper.class);

    @Mapping(source = "student.id", target = "studentId")
    @Mapping(source = "topicQuiz.id", target = "topicId")
    @Mapping(source = "quizResponseAnswers", target = "answers")
    @Mapping(source = "startedAt", target = "startTime")
    @Mapping(source = "completedAt", target = "submitTime")
    @Mapping(target = "totalPoints", expression = "java(quizResponse.getQuizResponseAnswers().stream().map(a -> a.getMark().doubleValue()).reduce(0.0, Double::sum))")
    QuizResponseDto toDto(QuizResponse quizResponse);

    @Mapping(source = "question", target = "questionId")
    @Mapping(source = "answer", target = "answerText")
    @Mapping(target = "correct", expression = "java(answer.getMark() != null && answer.getMark().compareTo(java.math.BigDecimal.ZERO) > 0)")
    @Mapping(source = "mark", target = "points")
    QuizResponseAnswerDto answerToDto(QuizResponseAnswer answer);

}