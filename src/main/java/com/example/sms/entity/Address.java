package com.example.sms.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

/**
 * A value object, not an entity: no id of its own, no separate table,
 * no independent lifecycle. @Embeddable classes are mapped into the
 * owning entity's table as plain columns via @Embedded there.
 *
 * Column names are left generic here; the owning entity can rename
 * them per-field using @AttributeOverride if the default name would
 * clash with something else in its table (see Student.java).
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Column(length = 100)
    private String street;

    @Column(length = 50)
    private String city;

    @Column(length = 50)
    private String state;

    @Column(length = 10)
    private String pincode;

    @Column(length = 50)
    private String country;
}
