package org.springframework.samples.petclinic.customers.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.customers.model.Owner;
import org.springframework.samples.petclinic.customers.model.OwnerRepository;
import org.springframework.samples.petclinic.customers.web.mapper.OwnerEntityMapper;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(OwnerResource.class)
@ActiveProfiles("test")
class OwnerResourceTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    OwnerRepository ownerRepository;

    @MockBean
    OwnerEntityMapper ownerEntityMapper;

    private Owner owner;
    private OwnerRequest ownerRequest;

    @BeforeEach
    void setup() {
        owner = createOwner(1, "John", "Doe", "123 Main St", "Anytown", "1234567890");

        ownerRequest = new OwnerRequest(
            "John",
            "Doe",
            "123 Main St",
            "Anytown",
            "1234567890"
        );
    }

    @Test
    void shouldGetAnOwnerInJsonFormat() throws Exception {
        given(ownerRepository.findById(1)).willReturn(Optional.of(owner));

        mvc.perform(get("/owners/1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.firstName").value("John"))
            .andExpect(jsonPath("$.lastName").value("Doe"))
            .andExpect(jsonPath("$.address").value("123 Main St"))
            .andExpect(jsonPath("$.city").value("Anytown"))
            .andExpect(jsonPath("$.telephone").value("1234567890"));
    }

    @Test
    void shouldReturnNotFoundForNonExistingOwner() throws Exception {
        given(ownerRepository.findById(999)).willReturn(Optional.empty());

        mvc.perform(get("/owners/999").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().string("null"));
    }

    @Test
    void shouldGetAllOwnersInJsonFormat() throws Exception {
        Owner owner2 = createOwner(2, "Jane", "Smith", "456 Oak St", "Othertown", "0987654321");

        List<Owner> owners = Arrays.asList(owner, owner2);
        given(ownerRepository.findAll()).willReturn(owners);

        mvc.perform(get("/owners").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.[0].id").value(1))
            .andExpect(jsonPath("$.[0].firstName").value("John"))
            .andExpect(jsonPath("$.[1].id").value(2))
            .andExpect(jsonPath("$.[1].firstName").value("Jane"));
    }

    @Test
    void shouldCreateNewOwner() throws Exception {
        given(ownerEntityMapper.map(any(Owner.class), eq(ownerRequest))).willReturn(owner);
        given(ownerRepository.save(any(Owner.class))).willReturn(owner);

        mvc.perform(post("/owners")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"John\",\"lastName\":\"Doe\",\"address\":\"123 Main St\",\"city\":\"Anytown\",\"telephone\":\"1234567890\"}")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    void shouldUpdateExistingOwner() throws Exception {
        given(ownerRepository.findById(1)).willReturn(Optional.of(owner));
        given(ownerEntityMapper.map(eq(owner), any(OwnerRequest.class))).willReturn(owner);

        mvc.perform(put("/owners/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"John\",\"lastName\":\"Doe\",\"address\":\"123 Main St\",\"city\":\"Anytown\",\"telephone\":\"1234567890\"}")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        verify(ownerRepository).save(owner);
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistingOwner() throws Exception {
        given(ownerRepository.findById(999)).willReturn(Optional.empty());

        mvc.perform(put("/owners/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"John\",\"lastName\":\"Doe\",\"address\":\"123 Main St\",\"city\":\"Anytown\",\"telephone\":\"1234567890\"}")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    private Owner createOwner(int id, String firstName, String lastName, String address, String city, String telephone) {
        Owner owner = new Owner();
        // Sử dụng reflection để đặt giá trị id
        try {
            java.lang.reflect.Field idField = Owner.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(owner, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set id via reflection", e);
        }
        owner.setFirstName(firstName);
        owner.setLastName(lastName);
        owner.setAddress(address);
        owner.setCity(city);
        owner.setTelephone(telephone);
        return owner;
    }
}