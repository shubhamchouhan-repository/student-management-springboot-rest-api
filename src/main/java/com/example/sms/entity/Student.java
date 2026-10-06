package com.example.sms.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "student")
@Setter
@Getter
@AllArgsConstructor
@RequiredArgsConstructor
@SuperBuilder
public class Student extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(nullable = false, length = 50)
    private String lastName;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 10)
    private String phoneNumber;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    // @Embedded pulls Address's columns straight into the students table
    // (address_line/street, city, state, pincode, country). We override
    // "street" here to show how a clashing/generic embeddable column name
    // can be renamed per usage site without touching the Address class.
    @Embedded
    @AttributeOverride(name = "street", column = @Column(name = "address_line", length = 100))
    private Address address;


    // Only the generated file name is stored in DB.
    // Actual image is stored on disk.
    @Column(name = "profile_image_name", length = 255)
    private String profileImageName;

    // Many Students -> One Department
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    // Many Students <-> Many Courses (join table: student_course)
    @ManyToMany
    @JoinTable(
            name = "student_course",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    @Builder.Default
    private Set<Course> courses = new HashSet<>();

}
