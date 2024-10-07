package com.triptune.domain.schedule.service;

import com.triptune.domain.BaseTest;
import com.triptune.domain.common.entity.*;
import com.triptune.domain.member.entity.Member;
import com.triptune.domain.member.repository.MemberRepository;
import com.triptune.domain.schedule.dto.CreateScheduleRequest;
import com.triptune.domain.schedule.dto.CreateScheduleResponse;
import com.triptune.domain.schedule.dto.ScheduleResponse;
import com.triptune.domain.schedule.entity.TravelAttendee;
import com.triptune.domain.schedule.entity.TravelSchedule;
import com.triptune.domain.schedule.enumclass.AttendeePermission;
import com.triptune.domain.schedule.enumclass.AttendeeRole;
import com.triptune.domain.schedule.repository.AttendeeRepository;
import com.triptune.domain.schedule.repository.ScheduleRepository;
import com.triptune.domain.travel.entity.TravelImage;
import com.triptune.domain.travel.entity.TravelPlace;
import com.triptune.domain.travel.repository.TravelRepository;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.exception.DataNotFoundException;
import com.triptune.global.util.PageableUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ScheduleServiceTests extends BaseTest {

    @InjectMocks
    private ScheduleService scheduleService;

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private AttendeeRepository attendeeRepository;

    @Mock
    private TravelRepository travelRepository;


    @Test
    @DisplayName("일정 만들기 성공")
    void createSchedule_success(){
        // given
        String userId = "test";
        CreateScheduleRequest request = createScheduleRequest();
        TravelSchedule savedtravelSchedule = createTravelSchedule();
        Member savedMember = createMember(1L, userId);

        when(scheduleRepository.save(any())).thenReturn(savedtravelSchedule);
        when(memberRepository.findByUserId(any())).thenReturn(Optional.of(savedMember));

        // when
        CreateScheduleResponse response = scheduleService.createSchedule(request, userId);

        // then
        verify(attendeeRepository, times(1)).save(any(TravelAttendee.class));
        assertEquals(response.getScheduleId(), savedtravelSchedule.getScheduleId());

    }

    @Test
    @DisplayName("일정 만들기 실패: 저장된 사용자 정보 없을 경우")
    void createSchedule_CustomUsernameNotFoundException(){
        // given
        CreateScheduleRequest request = createScheduleRequest();
        TravelSchedule schedule = createTravelSchedule();

        when(scheduleRepository.save(any())).thenReturn(schedule);
        when(memberRepository.findByUserId(any())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class, () -> scheduleService.createSchedule(request, "test"));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.USER_NOT_FOUND.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.USER_NOT_FOUND.getMessage());

    }

    @Test
    @DisplayName("일정 조회 성공")
    void getSchedule_success(){
        // given
        Country country = createCountry();
        City city = createCity(country);
        District district = createDistrict(city, "중구");
        ApiCategory apiCategory = createApiCategory();
        TravelPlace travelPlace1 = createTravelPlace(country, city, district, apiCategory);
        TravelPlace travelPlace2 = createTravelPlace(country, city, district, apiCategory);

        File file1 = createFile(1L, "test1", true);
        File file2 = createFile(2L, "test2", false);

        List<TravelImage> travelImageList1 = createTravelImages(travelPlace1, file1, file2);
        travelPlace1.setTravelImageList(travelImageList1);
        List<TravelImage> travelImageList2 = createTravelImages(travelPlace2, file1, file2);
        travelPlace2.setTravelImageList(travelImageList2);

        List<TravelPlace> placeList = createTravelPlaceList(travelPlace1, travelPlace2);

        TravelSchedule schedule = createTravelSchedule();
        Member member1 = createMember(1L, "member1");
        Member member2 = createMember(2L, "member2");


        List<TravelAttendee> attendeeList = createTravelAttendeeList(member1, member2, schedule);

        Pageable pageable = PageableUtil.createPageRequest(1, 5);

        when(scheduleRepository.findByScheduleId(any())).thenReturn(Optional.of(schedule));
        when(travelRepository.findAllByAreaData(any(), anyString(), anyString(), anyString()))
                .thenReturn(new PageImpl<>(placeList, pageable, 1));
        when(attendeeRepository.findAllByTravelSchedule_ScheduleId(any())).thenReturn(attendeeList);

        // when
        ScheduleResponse response = scheduleService.getSchedule(schedule.getScheduleId(), 1);

        // then
        assertEquals(response.getScheduleName(), schedule.getScheduleName());
        assertEquals(response.getCreatedAt(), schedule.getCreatedAt());
        assertEquals(response.getAttendeeList().get(0).getUserId(), attendeeList.get(0).getMember().getUserId());
        assertEquals(response.getAttendeeList().get(0).getRole(), attendeeList.get(0).getRole().name());
        assertEquals(response.getPlaceList().getTotalElements(), placeList.size());
        assertEquals(response.getPlaceList().getContent().get(0).getPlaceName(), placeList.get(0).getPlaceName());
        assertNotNull(response.getPlaceList().getContent().get(0).getThumbnailUrl());
    }

    @Test
    @DisplayName("일정 조회 성공: 여행지 데이터 없는 경우")
    void getScheduleWithoutPlaceList_success(){
        // given
        Country country = createCountry();
        City city = createCity(country);
        District district = createDistrict(city, "중구");
        ApiCategory apiCategory = createApiCategory();

        TravelSchedule schedule = createTravelSchedule();
        Member member1 = createMember(1L, "member1");
        Member member2 = createMember(2L, "member2");

        List<TravelAttendee> attendeeList = createTravelAttendeeList(member1, member2, schedule);

        Pageable pageable = PageableUtil.createPageRequest(1, 5);

        when(scheduleRepository.findByScheduleId(any())).thenReturn(Optional.of(schedule));
        when(travelRepository.findAllByAreaData(any(), anyString(), anyString(), anyString()))
                .thenReturn(new PageImpl<>(new ArrayList<>(), pageable, 0));
        when(attendeeRepository.findAllByTravelSchedule_ScheduleId(any())).thenReturn(attendeeList);

        // when
        ScheduleResponse response = scheduleService.getSchedule(schedule.getScheduleId(), 1);

        // then
        assertEquals(response.getScheduleName(), schedule.getScheduleName());
        assertEquals(response.getCreatedAt(), schedule.getCreatedAt());
        assertEquals(response.getAttendeeList().get(0).getUserId(), attendeeList.get(0).getMember().getUserId());
        assertEquals(response.getAttendeeList().get(0).getRole(), attendeeList.get(0).getRole().name());
        assertEquals(response.getPlaceList().getTotalElements(), 0);
        assertTrue(response.getPlaceList().getContent().isEmpty());
    }

    @Test
    @DisplayName("일정 조회 실패: 일정을 찾을 수 없는 경우")
    void getSchedule_notFoundException(){
        // given
        when(scheduleRepository.findByScheduleId(any())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class, () -> scheduleService.getSchedule(0L, 1));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.DATA_NOT_FOUND.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.DATA_NOT_FOUND.getMessage());
    }

}
