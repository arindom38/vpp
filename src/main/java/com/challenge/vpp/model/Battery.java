package com.challenge.vpp.model;

import jakarta.persistence.Entity;
import lombok.*;


@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Battery extends BaseEntity {
    private String name;
    private int postcode;
    private long wattCapacity;
}
