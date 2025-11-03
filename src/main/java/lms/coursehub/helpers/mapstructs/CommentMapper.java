package lms.coursehub.helpers.mapstructs;

import lms.coursehub.models.dtos.comment.CommentResponseDto;
import lms.coursehub.models.entities.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.username", target = "userName")
    @Mapping(source = "user.email", target = "userEmail")
    @Mapping(source = "topic.id", target = "topicId")
    CommentResponseDto toDto(Comment comment);
}
