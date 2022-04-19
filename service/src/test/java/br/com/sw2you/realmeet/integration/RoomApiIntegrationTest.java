package br.com.sw2you.realmeet.integration;

import static br.com.sw2you.realmeet.utils.TestDataCreator.newCreateRoomDTO;
import static br.com.sw2you.realmeet.utils.TestDataCreator.newRoomBuilder;
import static br.com.sw2you.realmeet.utils.TestsConstants.DEFAULT_ROOM_ID;
import static br.com.sw2you.realmeet.utils.TestsConstants.TEST_CLIENT_API_KEY;
import static org.junit.jupiter.api.Assertions.*;

import br.com.sw2you.realmeet.api.facade.RoomApi;
import br.com.sw2you.realmeet.api.model.CreateRoomDTO;
import br.com.sw2you.realmeet.api.model.UpdateRoomDTO;
import br.com.sw2you.realmeet.core.BaseIntegrationTest;
import br.com.sw2you.realmeet.domain.repository.RoomRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.HttpClientErrorException;

class RoomApiIntegrationTest extends BaseIntegrationTest {
    @Autowired
    RoomApi api;

    @Autowired
    RoomRepository roomRepository;

    @Override
    protected void setupEach() throws Exception {
        setLocalHostBasePath(api.getApiClient(), "/v1");
    }

    @Test
    void testGetRoomSuccess() {
        var room = newRoomBuilder().build();
        roomRepository.saveAndFlush(room);

        assertNotNull(room.getId());
        assertTrue(room.getActive());

        var dto = api.getRoom(TEST_CLIENT_API_KEY, room.getId());

        assertEquals(room.getId(), dto.getId());
        assertEquals(room.getName(), dto.getName());
        assertEquals(room.getSeats(), dto.getSeats());
    }

    @Test
    void testGetRoomInactive() {
        var room = newRoomBuilder().active(false).build();
        roomRepository.saveAndFlush(room);

        assertFalse(room.getActive());
        assertThrows(HttpClientErrorException.NotFound.class, () -> api.getRoom(TEST_CLIENT_API_KEY, room.getId()));
    }

    @Test
    void testGetRoomDoesNotExist() {
        assertThrows(HttpClientErrorException.NotFound.class, () -> api.getRoom(TEST_CLIENT_API_KEY, DEFAULT_ROOM_ID));
    }

    @Test
    void testCreateRoomSuccess() {
        var createRoomDTO = newCreateRoomDTO();
        var roomDTO = api.createRoom(TEST_CLIENT_API_KEY, createRoomDTO);

        assertEquals(roomDTO.getName(), createRoomDTO.getName());
        assertEquals(roomDTO.getSeats(), createRoomDTO.getSeats());
        assertNotNull(roomDTO.getId());

        var room = roomRepository.findById(roomDTO.getId()).orElseThrow();

        assertEquals(room.getName(), roomDTO.getName());
        assertEquals(room.getSeats(), roomDTO.getSeats());
    }

    @Test
    void testCreateRoomValidationError() {
        assertThrows(
            HttpClientErrorException.UnprocessableEntity.class,
            () -> api.createRoom(TEST_CLIENT_API_KEY, (CreateRoomDTO) newCreateRoomDTO().name(null))
        );
    }

    @Test
    void testDeleteRoomSuccess() {
        var roomId = roomRepository.saveAndFlush(newRoomBuilder().build()).getId();
        api.deleteRoom(TEST_CLIENT_API_KEY, roomId);
        assertFalse(roomRepository.findById(roomId).orElseThrow().getActive());
    }

    @Test
    void testDeleteRoomDoesNotExist() {
        assertThrows(HttpClientErrorException.NotFound.class, () -> api.deleteRoom(TEST_CLIENT_API_KEY, 1L));
    }

    @Test
    void testUpdateRoomSuccess() {
        var room = roomRepository.saveAndFlush(newRoomBuilder().build());
        var updateRoomDTO = new UpdateRoomDTO().name(room.getName() + "_").seats(room.getSeats() + 1);

        api.updateRoom(TEST_CLIENT_API_KEY, room.getId(), updateRoomDTO);

        var updateRoom = roomRepository.findById(room.getId()).orElseThrow();
        assertEquals(updateRoomDTO.getName(), updateRoom.getName());
        assertEquals(updateRoomDTO.getSeats(), updateRoom.getSeats());
    }

    @Test
    void testUpdateRoomDoesNotExist() {
        assertThrows(
            HttpClientErrorException.NotFound.class,
            () -> api.updateRoom(TEST_CLIENT_API_KEY, 1L, new UpdateRoomDTO().name("Room").seats(10))
        );
    }

    @Test
    void testUpdateRoomValidationError() {
        var room = roomRepository.saveAndFlush(newRoomBuilder().build());
        assertThrows(
            HttpClientErrorException.UnprocessableEntity.class,
            () -> api.updateRoom(TEST_CLIENT_API_KEY, room.getId(), new UpdateRoomDTO().name(null).seats(10))
        );
    }
}
