package ru.practicum.ewm.main.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Location {

    @Column(name = "location_lat")
    private Float lat;

    @Column(name = "location_lon")
    private Float lon;
}
