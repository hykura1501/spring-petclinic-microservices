package org.springframework.samples.petclinic.customers.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.customers.model.Owner;
import org.springframework.samples.petclinic.customers.model.PetType;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CustomersServiceIntegrationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void contextLoads() {
        // Basic test to ensure the application context loads successfully
    }

    @Test
    void shouldGetAllOwners() {
        ResponseEntity<List<Owner>> response = restTemplate.exchange(
            "http://localhost:" + port + "/owners",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<Owner>>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<Owner> owners = response.getBody();
        assertThat(owners).isNotNull();
        // We know there should be at least 10 owners in the test data
        assertThat(owners.size()).isGreaterThanOrEqualTo(10);
    }

    @Test
    void shouldGetAllPetTypes() {
        ResponseEntity<List<PetType>> response = restTemplate.exchange(
            "http://localhost:" + port + "/petTypes",
            HttpMethod.GET,
            null, 
            new ParameterizedTypeReference<List<PetType>>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<PetType> petTypes = response.getBody();
        assertThat(petTypes).isNotNull();
        // We know there should be at least 6 pet types in the test data
        assertThat(petTypes.size()).isGreaterThanOrEqualTo(6);
    }

    @Test
    void shouldCreateRetrieveAndUpdateOwner() {
        // Create new owner
        OwnerRequest newOwnerRequest = new OwnerRequest(
            "John",
            "Doe",
            "123 Main St",
            "Anytown",
            "1234567890"
        );

        ResponseEntity<Owner> createResponse = restTemplate.postForEntity(
            "http://localhost:" + port + "/owners",
            newOwnerRequest,
            Owner.class
        );

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Owner createdOwner = createResponse.getBody();
        assertThat(createdOwner).isNotNull();
        assertThat(createdOwner.getFirstName()).isEqualTo("John");
        assertThat(createdOwner.getLastName()).isEqualTo("Doe");
        Integer ownerId = createdOwner.getId();

        // Retrieve the owner
        ResponseEntity<Owner> getResponse = restTemplate.getForEntity(
            "http://localhost:" + port + "/owners/" + ownerId,
            Owner.class
        );

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Owner retrievedOwner = getResponse.getBody();
        assertThat(retrievedOwner).isNotNull();
        assertThat(retrievedOwner.getId()).isEqualTo(ownerId);
        assertThat(retrievedOwner.getFirstName()).isEqualTo("John");

        // Update the owner
        OwnerRequest updateOwnerRequest = new OwnerRequest(
            "John",
            "Smith", // changed last name
            "123 Main St",
            "Anytown",
            "1234567890"
        );

        restTemplate.put(
            "http://localhost:" + port + "/owners/" + ownerId,
            updateOwnerRequest
        );

        // Verify the update
        ResponseEntity<Owner> updatedGetResponse = restTemplate.getForEntity(
            "http://localhost:" + port + "/owners/" + ownerId,
            Owner.class
        );

        Owner updatedOwner = updatedGetResponse.getBody();
        assertThat(updatedOwner).isNotNull();
        assertThat(updatedOwner.getLastName()).isEqualTo("Smith");
    }

    @Test
    void shouldGetAPetWithDetails() {
        // George Franklin's pet Leo has ID 1
        ResponseEntity<PetDetails> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/owners/*/pets/1",
            PetDetails.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        PetDetails pet = response.getBody();
        assertThat(pet).isNotNull();
        assertThat(pet.id()).isEqualTo(1);
        assertThat(pet.name()).isEqualTo("Leo");
        // Owner should be "George Franklin"
        assertThat(pet.owner()).contains("George");
        assertThat(pet.owner()).contains("Franklin");
    }
}