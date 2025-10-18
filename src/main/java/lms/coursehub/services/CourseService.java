package lms.coursehub.services;

import lms.coursehub.helpers.exceptions.CustomException;
import lms.coursehub.helpers.mapstructs.CourseMapper;
import lms.coursehub.models.dtos.course.CreateCourseRequest;
import lms.coursehub.models.dtos.course.CourseResponseDto;
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

import java.util.List;
import java.util.UUID;

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

    @Transactional // This method still miss array-fields in the request
    public void updateCourse(String id, UpdateCourseRequest request) {
        Course course = courseRepo.findById(id)
                .orElseThrow(() -> new CustomException("Course not found", HttpStatus.NOT_FOUND));

        if (request.getTitle() != null)
            course.setTitle(request.getTitle());
        if (request.getDescription() != null)
            course.setDescription(request.getDescription());
        if (request.getImageUrl() != null)
            course.setImageUrl(request.getImageUrl());
        if (request.getCategory() != null)
            course.setCategory(request.getCategory());
        if (request.getLevel() != null)
            course.setLevel(request.getLevel());
        if (request.getIsPublished() != null)
            course.setPublished(request.getIsPublished());

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
        course.getEnrollmentDetails().add(enrollmentDetail);

        courseRepo.save(course);
    }

    // GET /course/{id} - Get course by ID
    @Transactional(readOnly = true)
    public CourseResponseDto getCourseById(String courseId) {
        Course course = findCourseById(courseId);
        return courseMapper.toResponseDto(course);
    }

    // GET /course - Get all public courses or teacher courses
    @Transactional(readOnly = true)
    public List<CourseResponseDto> getPublicCourses() {
        List<Course> courses = courseRepo.findByIsPublishedTrue();
        return courses.stream()
                .map(courseMapper::toResponseDto)
                .toList();
    }

    // GET /course?userId={userId} - Get courses created by a specific teacher
    @Transactional(readOnly = true)
    public List<CourseResponseDto> getTeacherCourses(UUID userId) {
        List<Course> courses = courseRepo.findByCreatorId(userId);
        return courses.stream()
                .map(courseMapper::toResponseDto)
                .toList();
    }

}
