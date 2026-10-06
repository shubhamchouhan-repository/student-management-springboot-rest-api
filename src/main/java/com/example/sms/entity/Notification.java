package com.example.sms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;


/**
 * Deliberately minimal: a row here just PROVES a side effect happened
 * (no real email is sent). It's the stand-in for "notify the student"
 * in the approve/reject workflow, kept simple on purpose.
 */
@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Notification extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String recipientEmail;

    @Column(nullable = false, length = 255)
    private String message;
}
