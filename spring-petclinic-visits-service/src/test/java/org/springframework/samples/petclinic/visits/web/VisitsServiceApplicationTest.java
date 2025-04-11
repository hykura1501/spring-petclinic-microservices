package org.springframework.samples.petclinic.visits.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.samples.petclinic.visits.VisitsServiceApplication;

@SpringBootTest(classes = VisitsServiceApplication.class)
public class VisitsServiceApplicationTest {

    @Test
    public void main() {
        VisitsServiceApplication.main(new String[]{});
    }
}
