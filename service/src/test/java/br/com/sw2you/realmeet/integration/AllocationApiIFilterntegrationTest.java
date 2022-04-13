package br.com.sw2you.realmeet.integration;

import static br.com.sw2you.realmeet.utils.TestDataCreator.newAllocationBuilder;
import static br.com.sw2you.realmeet.utils.TestDataCreator.newRoomBuilder;
import static br.com.sw2you.realmeet.utils.TestsConstants.DEFAULT_ALLOCATION_SUBJECT;
import static org.junit.jupiter.api.Assertions.assertEquals;

import br.com.sw2you.realmeet.api.facade.AllocationApi;
import br.com.sw2you.realmeet.core.BaseIntegrationTest;
import br.com.sw2you.realmeet.domain.repository.AllocationRepository;
import br.com.sw2you.realmeet.domain.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;

class AllocationApiIFilterntegrationTest extends BaseIntegrationTest {
    @Autowired
    private AllocationApi api;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private AllocationRepository allocationRepository;

    @Override
    protected void setupEach() throws Exception {
        setLocalHostBasePath(api.getApiClient(), "/v1");
    }

    @Test
    void filterAllAllocations() {
        var room = roomRepository.saveAndFlush(newRoomBuilder().build());
        var allocation1 = allocationRepository.saveAndFlush(newAllocationBuilder(room).subject(DEFAULT_ALLOCATION_SUBJECT + 1).build());
        var allocation2 = allocationRepository.saveAndFlush(newAllocationBuilder(room).subject(DEFAULT_ALLOCATION_SUBJECT + 2).build());
        var allocation3 = allocationRepository.saveAndFlush(newAllocationBuilder(room).subject(DEFAULT_ALLOCATION_SUBJECT + 3).build());

        var allocationDTOList = api.listAllocations(null, null, null, null);

        assertEquals(3, allocationDTOList.size());
        assertEquals(allocation1.getSubject(), allocationDTOList.get(0).getSubject());
        assertEquals(allocation2.getSubject(), allocationDTOList.get(1).getSubject());
        assertEquals(allocation3.getSubject(), allocationDTOList.get(2).getSubject());
    }
}
