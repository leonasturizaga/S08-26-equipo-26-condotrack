package com.condotrack.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "visitors")
@Getter @Setter @NoArgsConstructor
public class Visitor extends BaseEntity {
    @Column(name = "first_name", nullable = false, length = 100) private String firstName;
    @Column(name = "last_name", nullable = false, length = 100) private String lastName;
    @Column(name = "document_type", length = 40) private String documentType;
    @Column(name = "document_number", length = 100) private String documentNumber;
    @Column(length = 50) private String phone;
    @Column(name = "company_name", length = 150) private String companyName;
    @Column(columnDefinition = "TEXT") private String notes;
}
