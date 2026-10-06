package com.example.sms.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;


/**
 * Shared base for every entity: the primary key and four auditing columns.
 * Subclasses get these fields automatically through inheritance
 * (@MappedSuperclass does NOT create its own table — its fields are added
 * as columns to each subclass's table).
 *
 * createdBy / updatedBy are populated via the AuditorAware<String> bean
 * (see config.DefaultAuditorAware). Until Spring Security is added, that
 * bean just returns a fixed "SYSTEM" value — once Security is in place,
 * it will be swapped to return the actual logged-in username instead.
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(nullable = false, updatable = false, length = 50)
    private String createdBy;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @LastModifiedBy
    @Column(nullable = false, length = 50)
    private String updatedBy;
}










/*
• Class Inheritance: @Builder does not support subclassing or inherited fields from a parent class. @SuperBuilder allows child classes to include fields from their superclasses in the builder chain.
• Hierarchy Requirement: @SuperBuilder requires every class in the inheritance chain (both parents and children) to be explicitly annotated with @SuperBuilder.
• Compatibility: @SuperBuilder is not compatible with regular @Builder on the same class hierarchy; you cannot mix them across parent-child boundaries.
• Constructor Generation: @Builder relies on standard or explicit constructors/all-args constructors depending on configuration, while @SuperBuilder generates specialized protected constructors and internal generic builder classes to handle type-safe chaining up the hierarchy.
When to Use Which
• Use @Builder for standalone, independent classes that do not use class extension or inheritance.
• Use @SuperBuilder when you have a parent-child class structure and need to build instances while setting properties defined at multiple levels of the inheritance tree.

*/