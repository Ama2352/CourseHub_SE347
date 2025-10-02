package lms.coursehub.services;

import lms.coursehub.helpers.exceptions.CustomException;
import lms.coursehub.helpers.mapstructs.CourseMapper;
import lms.coursehub.models.dtos.course.CreateCourseRequest;
import lms.coursehub.models.dtos.course.UpdateCourseRequest;
import lms.coursehub.models.entities.Course;
import lms.coursehub.models.entities.EnrollmentDetail;
import lms.coursehub.models.entities.User;
import lms.coursehub.repositories.CourseRepo;
import lms.coursehub.repositories.EnrollmentDetailRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepo courseRepo;
    private final UserService userService;
    private final CourseMapper courseMapper;
    private final EnrollmentDetailRepo enrollmentDetailRepo;

    public Course findCourseById(String id) {
        return courseRepo.findById(id).orElseThrow(() -> new CustomException("Course not found", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public void createCourse(CreateCourseRequest request) {
        if (courseRepo.existsByTitle(request.getTitle()))
            throw new CustomException("A course with this name already exists. Please choose a different name",
                    HttpStatus.BAD_REQUEST);

        Course course = courseMapper.toEntity(request);
        course.setCreator(userService.getCurrentUser());

        courseRepo.save(course);
    }

    @Transactional
    public void updateCourse(String id, UpdateCourseRequest request) {
        Course course = courseRepo.findById(id)
                .orElseThrow(() -> new CustomException("Course not found", HttpStatus.NOT_FOUND));

        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setImageUrl(request.getImageUrl());
        course.setCategory(request.getCategory());
        course.setLevel(request.getLevel());
        course.setPublished(request.isPublished());

        courseRepo.save(course);
    }

    @Transactional
    public void addUserToCourse(String courseId) {
        User currentUser = userService.getCurrentUser();
        Course course = findCourseById(courseId);

        if (enrollmentDetailRepo.existsByStudentIdAndCourseId(currentUser.getId(), course.getId())) {
            throw new CustomException("User is already enrolled in this course", HttpStatus.BAD_REQUEST);
        }

        EnrollmentDetail enrollmentDetail = new EnrollmentDetail();
        enrollmentDetail.setCourse(course);
        enrollmentDetail.setStudent(currentUser);
        course.setTotalJoined(course.getTotalJoined() + 1);

        courseRepo.save(course);
    }
}
