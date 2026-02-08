package com.studentdata.service;

import com.studentdata.entity.Student;
import com.studentdata.repository.StudentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportService {

    private final StudentRepository studentRepository;

    public ReportService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public Page<Student> getStudents(int page, int size, String search, String studentClass) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());

        boolean hasSearch = search != null && !search.isEmpty();
        boolean hasClass = studentClass != null && !studentClass.isEmpty();

        if (hasSearch && hasClass) {
            return studentRepository.findByStudentIdContainingIgnoreCaseAndStudentClass(search, studentClass, pageable);
        } else if (hasSearch) {
            return studentRepository.findByStudentIdContainingIgnoreCase(search, pageable);
        } else if (hasClass) {
            return studentRepository.findByStudentClass(studentClass, pageable);
        }

        return studentRepository.findAll(pageable);
    }

    public List<String> getDistinctClasses() {
        return studentRepository.findDistinctClasses();
    }

    private List<Student> getFilteredStudents(String search, String studentClass) {
        boolean hasSearch = search != null && !search.isEmpty();
        boolean hasClass = studentClass != null && !studentClass.isEmpty();

        if (hasSearch && hasClass) {
            return studentRepository.findByStudentIdContainingIgnoreCaseAndStudentClass(search, studentClass);
        } else if (hasSearch) {
            return studentRepository.findByStudentIdContainingIgnoreCase(search);
        } else if (hasClass) {
            return studentRepository.findByStudentClass(studentClass);
        }

        return studentRepository.findAll();
    }
}
