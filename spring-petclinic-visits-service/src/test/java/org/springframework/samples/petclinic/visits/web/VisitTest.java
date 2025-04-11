package org.springframework.samples.petclinic.visits.web;

import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.visits.model.Visit;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class VisitTest {

    @Test
    void testVisitProperties() {
        Visit visit = new Visit();
        visit.setId(1);
        visit.setPetId(2);
        visit.setDate(new Date());
        visit.setDescription("Regular check-up");

        assertThat(visit.getId()).isEqualTo(1);
        assertThat(visit.getPetId()).isEqualTo(2);
        assertThat(visit.getDate()).isNotNull();
        assertThat(visit.getDescription()).isEqualTo("Regular check-up");
    }
}
