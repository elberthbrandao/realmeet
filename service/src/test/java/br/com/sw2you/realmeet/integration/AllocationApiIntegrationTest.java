package br.com.sw2you.realmeet.integration;

import static br.com.sw2you.realmeet.util.DateUtils.now;
import static br.com.sw2you.realmeet.utils.TestDataCreator.*;
import static br.com.sw2you.realmeet.utils.TestsConstants.*;
import static org.junit.jupiter.api.Assertions.*;

import br.com.sw2you.realmeet.api.facade.AllocationApi;
import br.com.sw2you.realmeet.core.BaseIntegrationTest;
import br.com.sw2you.realmeet.domain.entity.Allocation;
import br.com.sw2you.realmeet.domain.repository.AllocationRepository;
import br.com.sw2you.realmeet.domain.repository.RoomRepository;
import br.com.sw2you.realmeet.service.AllocationService;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;

class AllocationApiIntegrationTest extends BaseIntegrationTest {
    @Autowired
    private AllocationApi api;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private AllocationRepository allocationRepository;

    @Autowired
    private AllocationService allocationService;

    @Override
    protected void setupEach() throws Exception {
        setLocalHostBasePath(api.getApiClient(), "/v1");
    }

    @Test
    void testCreateAllocationSuccess() {
        var room = roomRepository.saveAndFlush(newRoomBuilder().build());
        var createAllocationDTO = newCreateAllocationDTO().roomId(room.getId());
        var allocationDTO = api.createAllocation(createAllocationDTO);

        assertNotNull(allocationDTO.getId());
        assertEquals(room.getId(), allocationDTO.getRoomId());
        assertEquals(createAllocationDTO.getSubject(), allocationDTO.getSubject());
        assertEquals(createAllocationDTO.getEmployeeName(), allocationDTO.getEmployeeName());
        assertEquals(createAllocationDTO.getEmployeeEmail(), allocationDTO.getEmployeeEmail());
        assertTrue(createAllocationDTO.getStartAt().isEqual(allocationDTO.getStartAt()));
        assertTrue(createAllocationDTO.getEndAt().isEqual(allocationDTO.getEndAt()));
    }

    @Test
    void testCreateAllocationValidationError() {
        var room = roomRepository.saveAndFlush(newRoomBuilder().build());
        var createAllocationDTO = newCreateAllocationDTO().roomId(room.getId()).subject(null);

        assertThrows(
            HttpClientErrorException.UnprocessableEntity.class,
            () -> api.createAllocation(createAllocationDTO)
        );
    }

    @Test
    void testCreateAllocationWhenRoomDoesNotExist() {
        assertThrows(HttpClientErrorException.NotFound.class, () -> api.createAllocation(newCreateAllocationDTO()));
    }

    @Test
    void testeDeleteAllocationSuccess() {
        var room = roomRepository.saveAndFlush(newRoomBuilder().build());
        var allocation = allocationRepository.saveAndFlush(newAllocationBuilder(room).build());

        api.deleteAllocation(allocation.getId());

        assertFalse(allocationRepository.findById(allocation.getId()).isPresent());
    }

    @Test
    void testeDeleteAllocationInThePast() {
        var room = roomRepository.saveAndFlush(newRoomBuilder().build());
        var allocation = allocationRepository.saveAndFlush(
            newAllocationBuilder(room).startAt(now().minusDays(1)).endAt(now().minusDays(1).plusHours(1)).build()
        );

        assertThrows(
            HttpClientErrorException.UnprocessableEntity.class,
            () -> api.deleteAllocation(allocation.getId())
        );
    }

    @Test
    void testeDeleteAllocationDoesNotExist() {
        assertThrows(HttpClientErrorException.NotFound.class, () -> api.deleteAllocation(1L));
    }

    @Test
    void testUpdateAllocationSuccess() {
        var room = roomRepository.saveAndFlush(newRoomBuilder().build());
        var createAllocationDTO = newCreateAllocationDTO().roomId(room.getId());
        var allocationDTO = api.createAllocation(createAllocationDTO);

        var updateAllocationDTO = newUpdateAllocationDTO()
            .subject(DEFAULT_ALLOCATION_SUBJECT + "_")
            .startAt(DEFAULT_ALLOCATION_START_AT.plusDays(1))
            .endAt(DEFAULT_ALLOCATION_END_AT.plusDays(1));

        api.updateAllocation(allocationDTO.getId(), updateAllocationDTO);

        var allocation = allocationRepository.findById(allocationDTO.getId()).orElseThrow();

        assertEquals(updateAllocationDTO.getSubject(), allocation.getSubject());
        assertTrue(updateAllocationDTO.getStartAt().isEqual(allocation.getStartAt()));
        assertTrue(updateAllocationDTO.getEndAt().isEqual(allocation.getEndAt()));
    }

    @Test
    void testUpdateAllocationDoesNotExist() {
        assertThrows(HttpClientErrorException.NotFound.class, () -> api.updateAllocation(1L, newUpdateAllocationDTO()));
    }

    @Test
    void testUpdateAllocationValidationError() {
        var room = roomRepository.saveAndFlush(newRoomBuilder().build());
        var createAllocationDTO = newCreateAllocationDTO().roomId(room.getId());
        var allocationDTO = api.createAllocation(createAllocationDTO);

        assertThrows(
            HttpClientErrorException.UnprocessableEntity.class,
            () -> api.updateAllocation(allocationDTO.getId(), newUpdateAllocationDTO().subject(null))
        );
    }

    @Test
    void testFilterAllocationUsingPagination() {
        persistAllocations(15);
        ReflectionTestUtils.setField(allocationService, "maxLimit", 10);

        var allocationListPage1 = api.listAllocations(null, null, null, null, null, null, 0);

        var allocationListPage2 = api.listAllocations(null, null, null, null, null, null, 1);

        assertEquals(10, allocationListPage1.size());
        assertEquals(5, allocationListPage2.size());
    }

    @Test
    void testFilterAllocationUsingPaginationAndLimit() {
        persistAllocations(25);
        ReflectionTestUtils.setField(allocationService, "maxLimit", 50);

        var allocationListPage1 = api.listAllocations(null, null, null, null, null, 10, 0);

        var allocationListPage2 = api.listAllocations(null, null, null, null, null, 10, 1);

        var allocationListPage3 = api.listAllocations(null, null, null, null, null, 10, 2);

        assertEquals(10, allocationListPage1.size());
        assertEquals(10, allocationListPage2.size());
        assertEquals(5, allocationListPage3.size());
    }

    @Test
    void testFilterAllocationOrderByStartAtDesc() {
        var allocationList = persistAllocations(3);

        var allocationDTOList = api.listAllocations(null, null, null, null, "-startAt", null, null);

        assertEquals(3, allocationDTOList.size());
        assertEquals(allocationList.get(0).getId(), allocationDTOList.get(2).getId());
        assertEquals(allocationList.get(1).getId(), allocationDTOList.get(1).getId());
        assertEquals(allocationList.get(2).getId(), allocationDTOList.get(0).getId());
    }

    @Test
    void testFilterAllocationOrderByInvalidField() {
        assertThrows(
            HttpClientErrorException.UnprocessableEntity.class,
            () -> api.listAllocations(null, null, null, null, "invalid", null, null)
        );
    }

    private List<Allocation> persistAllocations(int numberOfAllocations) {
        var room = roomRepository.saveAndFlush(newRoomBuilder().build());

        return IntStream
                .range(0, numberOfAllocations)
                .mapToObj(
                    i ->
                        allocationRepository.saveAndFlush(
                            newAllocationBuilder(room)
                                .subject(DEFAULT_ALLOCATION_SUBJECT + "_" + (i + 1))
                                .startAt(DEFAULT_ALLOCATION_START_AT.plusHours(i + 1))
                                .endAt(DEFAULT_ALLOCATION_END_AT.plusHours(i + 1))
                                .build())
            )
                .collect(Collectors.toList());
    }
}
