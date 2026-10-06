package com.example.sms.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentRequestDto {

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be 2 to 50 character")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must up to 50 character")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    @Size(max = 100)
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Phone number must be a valid 10 digit")
    private String phoneNumber;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be is the past")
    private LocalDate dateOfBirth;

    // Address fields are flattened here rather than nesting an AddressDto —
    // simpler for the client, and we still map them into the embedded
    // Address value object inside the service layer.
    @Size(max = 100)
    private String street;

    @Size(max = 50)
    private String city;

    @Size(max = 50)
    private String state;

    @Size(max = 10)
    private String pincode;

    @Size(max = 50)
    private String country;

    // Optional: a student can be created before being assigned anywhere
    private Long departmentId;

    // Optional: enroll in courses at creation time, or leave empty and
    // enroll later through a dedicated endpoint
    private Set<Long> courseIds;

}
