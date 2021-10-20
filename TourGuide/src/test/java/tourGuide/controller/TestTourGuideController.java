package tourGuide.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tourGuide.exception.BadRequestException;
import tourGuide.model.DTO.UserPreferencesDTO;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class TestTourGuideController {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void indexTest() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Greetings from TourGuide!")));
    }

    @Test
    public void getLocationTest() throws Exception {
        mockMvc.perform(get("/getLocation").param("userName", "internalUser0"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("longitude")))
                .andExpect(content().string(containsString("latitude")));
    }

    @Test
    public void getLocationBadUserNameTest() throws Exception {
        mockMvc.perform(get("/getLocation").param("userName", "internal0"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getNearByAttractionTest() throws Exception {
        mockMvc.perform(get("/getNearbyAttractions").param("userName", "internalUser0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(5)))
                .andExpect(content().string(containsString("attractionName")));
    }

    @Test
    public void getTripDealsTest() throws Exception {
        mockMvc.perform(get("/getTripDeals").param("userName", "internalUser0").param("attractionUUID", "99c251a1-7f7b-4804-a0a9-0da8b1bb50d3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(5)))
                .andExpect(content().string(containsString("tripId")));
    }

    @Test
    public void getTripDealsBadRequestTest() throws Exception {
        mockMvc.perform(get("/getTripDeals").param("userName", "internalUser0").param("attractionUUID", "test"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getRewardsBadRequestTest() throws Exception {
        mockMvc.perform(get("/getRewards").param("userName", ""))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof BadRequestException))
                .andExpect(result -> assertEquals("getRewards : userName is mandatory", result.getResolvedException().getMessage()));
    }

    @Test
    public void getUserPreferenceTest() throws Exception {
        mockMvc.perform(get("/userPreferences").param("userName", "internalUser0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("tripDuration", notNullValue()))
                .andExpect(jsonPath("ticketQuantity", notNullValue()))
                .andExpect(jsonPath("numberOfAdults", notNullValue()))
                .andExpect(jsonPath("numberOfChildren", notNullValue()))
                .andExpect(jsonPath("currency", notNullValue()))
                .andExpect(jsonPath("attractionProximity", notNullValue()))
                .andExpect(jsonPath("lowerPricePoint", notNullValue()))
                .andExpect(jsonPath("highPricePoint", notNullValue()));
    }

    @Test
    public void updateUserPreferenceTest() throws Exception {
    UserPreferencesDTO userPreferences = new UserPreferencesDTO();
		userPreferences.setTripDuration(7);
		userPreferences.setNumberOfChildren(3);
		userPreferences.setNumberOfAdults(2);
		userPreferences.setAttractionProximity(10);
		userPreferences.setCurrency("EUR");
		userPreferences.setHighPricePoint(500);
		userPreferences.setLowerPricePoint(100);
		userPreferences.setTicketQuantity((5));

        RequestBuilder updateRequest = MockMvcRequestBuilders
                .put("/userPreferences").param("userName", "internalUser0")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(userPreferences));

        mockMvc.perform(updateRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("userPreferences.tripDuration", is(7)))
                .andExpect(jsonPath("userPreferences.ticketQuantity", is(5)))
                .andExpect(jsonPath("userPreferences.numberOfAdults", is(2)))
                .andExpect(jsonPath("userPreferences.numberOfChildren", is(3)))
                .andExpect(jsonPath("userPreferences.lowerPricePoint.currency.currencyCode", containsString("EUR")))
                .andExpect(jsonPath("userPreferences.lowerPricePoint.number", is(100)));

    }
}
