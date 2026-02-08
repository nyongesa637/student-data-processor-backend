package com.studentdata.repository;

import com.studentdata.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Page<Student> findByStudentIdContainingIgnoreCase(String studentId, Pageable pageable);

    Page<Student> findByStudentClass(String studentClass, Pageable pageable);

    Page<Student> findByStudentIdContainingIgnoreCaseAndStudentClass(String studentId, String studentClass, Pageable pageable);

    @Query("SELECT DISTINCT s.studentClass FROM Student s ORDER BY s.studentClass")
    List<String> findDistinctClasses();

    List<Student> findByStudentIdContainingIgnoreCaseAndStudentClass(String studentId, String studentClass);

    List<Student> findByStudentIdContainingIgnoreCase(String studentId);

    List<Student> findByStudentClass(String studentClass);
}
