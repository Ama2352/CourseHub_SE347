package lms.coursehub.helpers.mapstructs;

import lms.coursehub.models.dtos.notification.NotificationDto;
import lms.coursehub.models.entities.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "message", target = "message")
    @Mapping(source = "timestamp", target = "timestamp")
    @Mapping(source = "isRead", target = "isRead")
    NotificationDto toDto(Notification notification);

    List<NotificationDto> toDtos(List<Notification> list);
}
