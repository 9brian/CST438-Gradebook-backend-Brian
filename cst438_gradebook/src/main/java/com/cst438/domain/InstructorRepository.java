package com.cst438.domain;


import org.springframework.data.repository.CrudRepository;


public interface InstructorRepository extends CrudRepository<Instructor, Integer>{
//    Instructor findByAlias(String alias);
    Instructor findByEmail(String email);
}
