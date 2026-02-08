package com.studentdata.dto;

public class StudentDto {
    private Long id;
    private String studentId;
    private String firstName;
    private String lastName;
    private String dob;
    private String studentClass;
    private int score;

    public StudentDto() {}

    public StudentDto(Long id, String studentId, String firstName, String lastName, String dob, String studentClass, int score) {
        this.id = id;
        this.studentId = studentId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dob = dob;
        this.studentClass = studentClass;
        this.score = score;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }

    public String getStudentClass() { return studentClass; }
    public void setStudentClass(String studentClass) { this.studentClass = studentClass; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
}
