package com.cst438.domain;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface AssignmentRepository extends CrudRepository <Assignment, Integer> {

	@Query("select a from Assignment a where a.course.instructor= :email order by a.id")
	List<Assignment> findByEmail(@Param("email") String email);

	@Query("select a from Assignment a where a.id =:assignmentId")
	Assignment findByAssignmentId(@Param("assignmentId") int assignmentId);

	@Query("select a from Course a where a.instructor =:email")
	Course findCourseByInstructorEmail(@Param("email") String email);
}
