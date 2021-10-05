package br.com.sw2you.realmeet.integration;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import br.com.sw2you.realmeet.api.facade.RoomApi;
import br.com.sw2you.realmeet.core.BaseIntegrationTest;
import br.com.sw2you.realmeet.domain.pository.RoomRepository;
import br.com.sw2you.realmeet.utils.TestDataCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;

class RoomApiIntegrationTest extends BaseIntegrationTest {

    @Autowired
    RoomApi roomApi;

    @Autowired
    RoomRepository roomRepository;

    @Override
    protected void setupEach() throws Exception{
        setLocalHostBasePath(roomApi.getApiClient(), "/v1");
    }

    @Test
    void testGetRoomSuccess() {
        var room = TestDataCreator.newRoomBuilder().build();
        roomRepository.saveAndFlush(room);

        assertNotNull(room.getId());
        assertTrue(room.getActive());

        var dto = roomApi.getRoom(room.getId());

        assertEquals(room.getId(), dto.getId());
        assertEquals(room.getName(), dto.getName());
        assertEquals(room.getSeats(), dto.getSeats());
    }
}
