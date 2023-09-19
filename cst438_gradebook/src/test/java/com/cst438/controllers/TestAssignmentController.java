package com.cst438.controllers;

import com.cst438.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.sql.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/*
 * Example of using Junit
 * Mockmvc is used to test a simulated REST call to the RestController
 * This test assumes that students test4@csumb.edu, test@csumb.edu are enrolled in course
 * with assignment with id=1
 */
@SpringBootTest
@AutoConfigureMockMvc
public class TestAssignmentController {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private AssignmentGradeRepository assignmentGradeRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Test
    public void testCreateAssignment() throws Exception {

        MockHttpServletResponse response;

        // Create new dto object
        AssignmentDTO dto = new AssignmentDTO(3, "test", "2023-01-01", "CST 363 - Introduction to Database Systems", 31045);
        // Post dto object
        response = mvc.perform(MockMvcRequestBuilders.post("/assignment/new")
                        .content(asJsonString(dto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();
        assertEquals(200, response.getStatus()); // return status

        // retrieve created object
        response = mvc.perform(MockMvcRequestBuilders.get("/assignment/3")
                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();
        assertEquals(200, response.getStatus()); // return status

        // Check if assignment was saved into repository
        Assignment assignment = assignmentRepository.findById(3).get();

        // Create dto off of object
        AssignmentDTO aDTO = new AssignmentDTO(
                assignment.getId(),
                assignment.getName(),
                assignment.getDueDate() + "",
                assignment.getCourse().getTitle(),
                assignment.getCourse().getCourse_id()
        );

        // Check if original dto is equal to fetched dto
        assertEquals(dto, aDTO);
    }

    @Test
    public void testUpdateAssignment() throws Exception {
        MockHttpServletResponse response;

        // find object before the update
        Assignment beforeUpdate = assignmentRepository.findById(1).get();
        AssignmentDTO beforeUpdateDTO = new AssignmentDTO(
                beforeUpdate.getId(),
                beforeUpdate.getName(),
                beforeUpdate.getDueDate() + "",
                beforeUpdate.getCourse().getTitle(),
                beforeUpdate.getCourse().getCourse_id()
        );

        // Create new dto object
        AssignmentDTO dto = new AssignmentDTO(1, "test", "2023-01-01", "CST 363 - Introduction to Database Systems", 31045);
        // Post dto object
        response = mvc.perform(MockMvcRequestBuilders.put("/assignment/update/1")
                        .content(asJsonString(dto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertEquals(200, response.getStatus()); // return status

        // retrieve created object
        response = mvc.perform(MockMvcRequestBuilders.get("/assignment/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertEquals(200, response.getStatus()); // return status

        // fetch object after update
        // Check if assignment was saved into repository
        Assignment assignment = assignmentRepository.findById(1).get();

        // Create dto off of object
        AssignmentDTO afterUpdateDTO = new AssignmentDTO(
                assignment.getId(),
                assignment.getName(),
                assignment.getDueDate() + "",
                assignment.getCourse().getTitle(),
                assignment.getCourse().getCourse_id()
        );
        // Check if original dto is equal to fetched dto
        assertNotEquals(beforeUpdateDTO, afterUpdateDTO);
    }

    @Test
    public void testRetrieveAssignment() throws Exception {
        MockHttpServletResponse response;

        response = mvc.perform(MockMvcRequestBuilders.get("/assignment/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertEquals(200, response.getStatus()); // return status

        // Check if assignment was saved into repository
        Assignment assignment = assignmentRepository.findById(1).get();

        // Create dto off of object
        AssignmentDTO a = new AssignmentDTO(
                assignment.getId(),
                assignment.getName(),
                assignment.getDueDate() + "",
                assignment.getCourse().getTitle(),
                assignment.getCourse().getCourse_id()
        );
        // Check if original dto is equal to fetched dto
        assertNotNull(a);
    }

    @Test
    public void testDeleteAssignment() throws Exception {
        MockHttpServletResponse response;

        response = mvc.perform(MockMvcRequestBuilders.get("/assignment/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertEquals(200, response.getStatus()); // return status

        // Check if assignment was saved into repository
        Assignment beforeDelete = assignmentRepository.findById(1).get();

        // Create dto off of object
        AssignmentDTO a = new AssignmentDTO(
                beforeDelete.getId(),
                beforeDelete.getName(),
                beforeDelete.getDueDate() + "",
                beforeDelete.getCourse().getTitle(),
                beforeDelete.getCourse().getCourse_id()
        );

        response = mvc.perform(MockMvcRequestBuilders.delete("/assignment/delete/1?false=true")
                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();
        assertEquals(400, response.getStatus()); // grades should exist

        response = mvc.perform(MockMvcRequestBuilders.delete("/assignment/delete/2?force=true")
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        assertEquals(200, response.getStatus());

    }


    private static String asJsonString(final Object obj) {
        try {

            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> T fromJsonString(String str, Class<T> valueType) {
        try {
            return new ObjectMapper().readValue(str, valueType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}