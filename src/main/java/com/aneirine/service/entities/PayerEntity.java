package com.aneirine.service.entities;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "payers")
public class PayerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String email;
    private String firstName;
    private String lastName;
    private String paypalId;
    private String countryCode;

    @OneToMany
    private List<PaymentEntity> payments;

}
