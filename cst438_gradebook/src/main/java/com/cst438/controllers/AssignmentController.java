package com.cst438.controllers;

import java.sql.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.cst438.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

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

	String instructorEmail = "dwisneski@csumb.edu";
	
	@GetMapping("/assignment")
	public AssignmentDTO[] getAllAssignmentsForInstructor() {
		// get all assignments for this instructor
		String instructorEmail = "dwisneski@csumb.edu";  // user name (should be instructor's email) 
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
	
	// TODO create CRUD methods for Assignment

	//	adding a new assignment
	@PostMapping("/assignment/new")
	public int createNewAssignment(
			@RequestBody AssignmentDTO dto) {
		// add check that instructor oversees course and assignment before modifications
		String instructorEmail = "dwisneski@csumb.edu";

		Assignment assignment = new Assignment();

		Course course = courseRepository.findById(dto.courseId()).get();
		assignment.setCourse(course);
		assignment.setName(dto.assignmentName());
		assignment.setDueDate(Date.valueOf(dto.dueDate()));

		assignmentRepository.save(assignment);

		return assignment.getId();
	}

	//	retrieve an assignment by id
	@GetMapping("/assignment/{id}")
	public AssignmentDTO getAssignmentByID( @PathVariable("id") int id ) {
		Optional<Assignment> assign = assignmentRepository.findById(id);
		Assignment assignment = assign.get();

		AssignmentDTO dto = new AssignmentDTO(
				assignment.getId(),                    //		int id,
				assignment.getName(),                    //		String assignmentName,
				assignment.getDueDate().toString(),        //		String dueDate,
				assignment.getCourse().getTitle(),        //		String courseTitle,
				assignment.getCourse().getCourse_id()); //		int courseId
//			System.out.println(dto.toString());
		return dto;
	}

//	update an assigment
	@PutMapping("/assignment/update/{id}")
	public int updateAssignment(
			@PathVariable("id") int id,
			@RequestBody AssignmentDTO dto) {
		// add check that instructor oversees course and assignment before modifications
		System.out.println(dto);
		Assignment a = assignmentRepository.findById(id).get();
		System.out.println(a);
//		Assignment assignment = new Assignment();

		if(dto.assignmentName() != null){
			a.setName(dto.assignmentName());
		}

		if (dto.dueDate() != null){
			a.setDueDate(Date.valueOf(dto.dueDate()));
		}

		if (dto.courseTitle() != null){
			Course course = courseRepository.findById(dto.courseId()).get();
			a.setCourse(course);
		}

		assignmentRepository.save(a);

		return dto.id();
	}

	//	delete an assignment
	@DeleteMapping("/assignment/delete/{id}")
	public void deleteAssignment(
			@PathVariable("id") int id,
			@RequestParam(required = false) boolean force) {
		// https://www.baeldung.com/spring-request-param
		// optional force parameter

		// check if assignments have grades added to them
		boolean grades = false;
		for (AssignmentGrade ag: assignmentGradeRepository.findAll()) {
//			System.out.println(ag.getAssignment().getId());
			int assignmentID = ag.getAssignment().getId();
			if (id == assignmentID) {
				grades = true;
				break;
			}
		}

		// grab assignment
		Assignment assignment = assignmentRepository.findById(id).get();

		if(!Objects.equals(instructorEmail, assignment.getCourse().getInstructor())){
			throw new ResponseStatusException( HttpStatus.FORBIDDEN , "Instructor did not create this assignment " + assignment.getName());
		}

		if(force || !grades){ // if force is present or grades do not exist
			assignmentRepository.deleteById(id);
		} else {
			throw new ResponseStatusException( HttpStatus.BAD_REQUEST , "Grades exist for this assignment " + assignment.getName());
		}

	}
}
