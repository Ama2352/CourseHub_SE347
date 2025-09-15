package lms.coursehub.helpers.mapstructs;

import lms.coursehub.helpers.utils.MappingUtils;
import lms.coursehub.models.dtos.user.UserProfileResponse;
import lms.coursehub.models.entities.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = MappingUtils.class)
public interface UserMapper {

    UserProfileResponse toDTO(User user);
}
