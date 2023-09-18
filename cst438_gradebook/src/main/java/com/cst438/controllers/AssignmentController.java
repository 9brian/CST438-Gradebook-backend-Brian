package com.cst438.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.cst438.domain.Assignment;
import com.cst438.domain.AssignmentDTO;
import com.cst438.domain.AssignmentRepository;
import com.cst438.domain.Course;
import com.cst438.domain.CourseRepository;

@RestController
@CrossOrigin 
public class AssignmentController {
	
	@Autowired
	AssignmentRepository assignmentRepository;
	
	@Autowired
	CourseRepository courseRepository;
	
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
	public AssignmentDTO createNewAssignment(
			@RequestBody Assignment newAssignment) {
		// TODO
		// add check that instructor oversees course and assignment before modifications
		String instructorEmail = "dwisneski@csumb.edu";
		Assignment assignment = newAssignment;

		int course_id = assignment.getCourse().getCourse_id();
		if (courseRepository.existsById(course_id)){
			Optional<Course> course = courseRepository.findById(course_id);
			Course foundCourse = course.get();

			if(newAssignment.getCourse().getTitle() == null){
				newAssignment.setCourse(foundCourse);
			}
		}

		assignmentRepository.save(assignment);

		AssignmentDTO dto = new AssignmentDTO(
				assignment.getId(),                    //		int id,
				assignment.getName(),                    //		String assignmentName,
				assignment.getDueDate().toString(),        //		String dueDate,
				assignment.getCourse().getTitle(),        //		String courseTitle,
				assignment.getCourse().getCourse_id()); //		int courseId
//			System.out.println(dto.toString());
		return dto;
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
	public AssignmentDTO updateAssignment(
			@PathVariable("id") int id,
			@RequestBody Assignment newAssignment) {
		// add check that instructor oversees course and assignment before modifications
		Optional<Assignment> assign = assignmentRepository.findById(id);

		System.out.println(assign);
		if (assign.isPresent()){
			Assignment assignment = assign.get();

			// handle when updates are pushed
			if(newAssignment.getDueDate() != null){ // Passed in due date
				assignment.setDueDate(newAssignment.getDueDate());
			}

			if(newAssignment.getName() != null){ // Passed in name
				assignment.setName(newAssignment.getName());
			}

			if(newAssignment.getCourse() != null){ // passed in
				assignment.setCourse(newAssignment.getCourse());
			}

			//		https://www.javaguides.net/2022/04/putmapping-spring-boot-example.html
			// how to save to repository
			assignmentRepository.save(assignment);
			AssignmentDTO dto = getAssignmentByID(id);

			return dto;
		}
		return null;
	}

//	delete an assignment
@DeleteMapping("/assignment/delete/{id}")
	public int deleteAssignment(
			@PathVariable("id") int id) {
		// TODO
		// add check that instructor oversees course and assignment before modifications

		if (assignmentRepository.existsById(id)){
			assignmentRepository.deleteById(id); // found through crud repository class
			return 200; // success
		} else {
			return 404; // not found
		}
	}
}
