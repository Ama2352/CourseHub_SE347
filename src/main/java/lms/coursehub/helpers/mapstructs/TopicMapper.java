package lms.coursehub.helpers.mapstructs;

import lms.coursehub.helpers.utils.MappingUtils;
import lms.coursehub.models.dtos.topic.AssignmentDataDto;
import lms.coursehub.models.dtos.topic.CloudinaryFileDto;
import lms.coursehub.models.dtos.topic.CreateTopicRequest;
import lms.coursehub.models.dtos.topic.FileDataDto;
import lms.coursehub.models.dtos.topic.LinkDataDto;
import lms.coursehub.models.dtos.topic.MeetingDataDto;
import lms.coursehub.models.dtos.topic.PageDataDto;
import lms.coursehub.models.dtos.topic.QuestionDto;
import lms.coursehub.models.dtos.topic.QuizDataDto;
import lms.coursehub.models.entities.CloudinaryFile;
import lms.coursehub.models.entities.Question;
import lms.coursehub.models.entities.Topic;
import lms.coursehub.models.entities.TopicAssignment;
import lms.coursehub.models.entities.TopicFile;
import lms.coursehub.models.entities.TopicLink;
import lms.coursehub.models.entities.TopicMeeting;
import lms.coursehub.models.entities.TopicPage;
import lms.coursehub.models.entities.TopicQuiz;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = MappingUtils.class)
public interface TopicMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "studentCount", ignore = true)
    @Mapping(target = "section", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "topicAssignment", ignore = true)
    Topic toEntity(CreateTopicRequest request);

    @Mapping(target = "questions", source = "questions")
    QuizDataDto toQuizDataDto(TopicQuiz topicQuiz);

    @Mapping(target = "choices", ignore = true)
    QuestionDto toQuestionDto(Question question);

    @Mapping(target = "assignmentFiles", source = "assignmentFiles")
    AssignmentDataDto toAssignmentDataDto(TopicAssignment topicAssignment);

    CloudinaryFileDto toCloudinaryFileDto(CloudinaryFile cloudinaryFile);

    LinkDataDto toLinkDataDto(TopicLink topicLink);

    PageDataDto toPageDataDto(TopicPage topicPage);

    MeetingDataDto toMeetingDataDto(TopicMeeting topicMeeting);

    @Mapping(source = "file", target = "file")
    FileDataDto toFileDataDto(TopicFile topicFile);
}
