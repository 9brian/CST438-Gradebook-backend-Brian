package com.cst438.controllers;

import java.sql.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.cst438.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@CrossOrigin 
public class AssignmentController {
	
	@Autowired
	AssignmentRepository assignmentRepository;
	
	@Autowired
	CourseRepository courseRepository;

	@Autowired
	AssignmentGradeRepository assignmentGradeRepository;

	@Autowired
	InstructorRepository instructorRepository;


	@GetMapping("/assignment")
	public AssignmentDTO[] getAllAssignmentsForInstructor() {
		// get authenticated user object
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		// get all assignments for this instructor
		String instructorEmail = (String) auth.getPrincipal();

		List<Assignment> assignments = assignmentRepository.findByEmail(instructorEmail);
		AssignmentDTO[] result = new AssignmentDTO[assignments.size()];
		for (int i=0; i<assignments.size(); i++) {
			Assignment as = assignments.get(i);
			AssignmentDTO dto = new AssignmentDTO(
					as.getId(), 
					as.getName(), 
					as.getDueDate().toString(), 
					as.getCourse().getTitle(), 
					as.getCourse().getCourse_id());
			result[i]=dto;
		}
		return result;
	}

	//	adding a new assignment
	@PostMapping("/assignment/new")
	public int createNewAssignment(
			@RequestBody AssignmentDTO dto) {

		// get authenticated user object
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		// get authenticated users email/name
		String currentUserEmail = (String) auth.getPrincipal();

		// Look for instructor based on dto
		String foundEmail = courseRepository.findById(dto.courseId()).get().getInstructor();

		// check if user logged in oversees/ is an instructor for course
		if (Objects.equals(currentUserEmail, foundEmail)){
			Assignment assignment = new Assignment();

			Course course = courseRepository.findById(dto.courseId()).get();
			assignment.setCourse(course);
			assignment.setName(dto.assignmentName());
			assignment.setDueDate(Date.valueOf(dto.dueDate()));

			assignmentRepository.save(assignment);

			return assignment.getId();
		}
		return -1;
	}

	//	retrieve an assignment by id
	@GetMapping("/assignment/{id}")
	public AssignmentDTO getAssignmentByID( @PathVariable("id") int id ) {
		// get authenticated user object
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		// get authenticated users email/name
		String currentUserEmail = (String) auth.getPrincipal();

		Optional<Assignment> assign = assignmentRepository.findById(id);
		Assignment assignment = assign.get();

		// look for instructor based on assignment
		String instructorEmail = assignment.getCourse().getInstructor();

		// Check if user logged in, is the instructor for the course
		if (Objects.equals(currentUserEmail, instructorEmail)){
			AssignmentDTO dto = new AssignmentDTO(
					assignment.getId(),                    //		int id,
					assignment.getName(),                    //		String assignmentName,
					assignment.getDueDate().toString(),        //		String dueDate,
					assignment.getCourse().getTitle(),        //		String courseTitle,
					assignment.getCourse().getCourse_id()); //		int courseId
			return dto;
		}
		return null;
	}

//	update an assigment
	@PutMapping("/assignment/update/{id}")
	public int updateAssignment(
			@PathVariable("id") int id,
			@RequestBody AssignmentDTO dto) {
		// get authenticated user object
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		// get authenticated users email/name
		String currentUserEmail = (String) auth.getPrincipal();

		Assignment a = assignmentRepository.findById(id).get();

		// look for instructor based on assignment
		String instructorEmail = a.getCourse().getInstructor();

		// Check if user logged in, is the instructor for the course
		if (Objects.equals(currentUserEmail, instructorEmail)) {

			if (dto.assignmentName() != null) {
				a.setName(dto.assignmentName());
			}

			if (dto.dueDate() != null) {
				a.setDueDate(Date.valueOf(dto.dueDate()));
			}

			if (dto.courseTitle() != null) {
				Course course = courseRepository.findById(dto.courseId()).get();
				a.setCourse(course);
			}

			assignmentRepository.save(a);

			return dto.id();

		}
		return -1;
	}

	//	delete an assignment
	@DeleteMapping("/assignment/delete/{id}")
	public void deleteAssignment(
			@PathVariable("id") int id,
			@RequestParam(required = false) boolean force) {
		// https://www.baeldung.com/spring-request-param
		// optional force parameter

		// get authenticated user object
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		// get authenticated users email/name
		String currentUserEmail = (String) auth.getPrincipal();

		// check if assignments have grades added to them
		boolean grades = false;
		for (AssignmentGrade ag: assignmentGradeRepository.findAll()) {
			int assignmentID = ag.getAssignment().getId();
			if (id == assignmentID) {
				grades = true;
				break;
			}
		}

		// grab assignment
		Assignment assignment = assignmentRepository.findById(id).get();

		if(!Objects.equals(currentUserEmail, assignment.getCourse().getInstructor())){
			throw new ResponseStatusException( HttpStatus.FORBIDDEN , "Instructor did not create this assignment " + assignment.getName());
		}

		if(force || !grades){ // if force is present or grades do not exist
			assignmentRepository.deleteById(id);
		} else {
			throw new ResponseStatusException( HttpStatus.BAD_REQUEST , "Grades exist for this assignment " + assignment.getName());
		}

	}
}
