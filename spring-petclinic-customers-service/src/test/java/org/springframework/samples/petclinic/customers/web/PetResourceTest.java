package org.springframework.samples.petclinic.customers.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.customers.model.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.text.SimpleDateFormat;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(PetResource.class)
@ActiveProfiles("test")
class PetResourceTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    PetRepository petRepository;

    @MockBean
    OwnerRepository ownerRepository;

    private Owner owner;
    private Pet pet;
    private PetType petType;
    private List<PetType> petTypes;
    private Date birthDate;

    @BeforeEach
    void setup() throws Exception {
        owner = new Owner();
        owner.setFirstName("George");
        owner.setLastName("Bush");
        owner.setAddress("123 Main St");
        owner.setCity("Anytown");
        owner.setTelephone("1234567890");

        petType = new PetType();
        petType.setId(6);
        petType.setName("dog");

        petTypes = Arrays.asList(petType);

        birthDate = new SimpleDateFormat("yyyy-MM-dd").parse("2020-01-01");

        pet = new Pet();
        pet.setId(2);
        pet.setName("Basil");
        pet.setType(petType);
        pet.setBirthDate(birthDate);
        owner.addPet(pet);
    }

    @Test
    void shouldGetAPetInJSonFormat() throws Exception {
        given(petRepository.findById(2)).willReturn(Optional.of(pet));

        mvc.perform(get("/owners/*/pets/2").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(2))
            .andExpect(jsonPath("$.name").value("Basil"))
            .andExpect(jsonPath("$.type.id").value(6))
            .andExpect(jsonPath("$.owner").value("George Bush"));
    }

    @Test
    void shouldGetAllPetTypes() throws Exception {
        given(petRepository.findPetTypes()).willReturn(petTypes);

        mvc.perform(get("/petTypes").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.[0].id").value(6))
            .andExpect(jsonPath("$.[0].name").value("dog"));
    }

    @Test
    void shouldCreateNewPet() throws Exception {
        given(ownerRepository.findById(1)).willReturn(Optional.of(owner));
        given(petRepository.findPetTypeById(6)).willReturn(Optional.of(petType));
        given(petRepository.save(any(Pet.class))).willAnswer(invocation -> invocation.getArgument(0));

        mvc.perform(post("/owners/1/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":null,\"name\":\"Fluffy\",\"birthDate\":\"2020-01-01\",\"typeId\":6}")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Fluffy"))
            .andExpect(jsonPath("$.birthDate").exists());
    }

    @Test
    void shouldReturnNotFoundWhenCreatingPetForNonExistingOwner() throws Exception {
        given(ownerRepository.findById(999)).willReturn(Optional.empty());

        mvc.perform(post("/owners/999/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":null,\"name\":\"Fluffy\",\"birthDate\":\"2020-01-01\",\"typeId\":6}")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateExistingPet() throws Exception {
        given(petRepository.findById(2)).willReturn(Optional.of(pet));
        given(petRepository.findPetTypeById(6)).willReturn(Optional.of(petType));
        given(petRepository.save(any(Pet.class))).willAnswer(invocation -> invocation.getArgument(0));

        mvc.perform(put("/owners/*/pets/{petId}", 2)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":2,\"name\":\"Fluffy\",\"birthDate\":\"2020-01-01\",\"typeId\":6}")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        verify(petRepository).save(any(Pet.class));
    }

    @Test
    void shouldReturnNotFoundWhenGettingNonExistingPet() throws Exception {
        given(petRepository.findById(999)).willReturn(Optional.empty());

        mvc.perform(get("/owners/*/pets/999")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }
}