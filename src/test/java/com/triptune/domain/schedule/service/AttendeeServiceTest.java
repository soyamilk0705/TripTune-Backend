package com.triptune.domain.schedule.service;

import com.triptune.domain.member.entity.Member;
import com.triptune.domain.member.repository.MemberRepository;
import com.triptune.domain.schedule.ScheduleTest;
import com.triptune.domain.schedule.dto.request.CreateAttendeeRequest;
import com.triptune.domain.schedule.entity.TravelAttendee;
import com.triptune.domain.schedule.entity.TravelSchedule;
import com.triptune.domain.schedule.enumclass.AttendeePermission;
import com.triptune.domain.schedule.enumclass.AttendeeRole;
import com.triptune.domain.schedule.exception.AlreadyAttendeeException;
import com.triptune.domain.schedule.exception.ForbiddenScheduleException;
import com.triptune.domain.schedule.repository.TravelAttendeeRepository;
import com.triptune.domain.schedule.repository.TravelScheduleRepository;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.exception.DataNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AttendeeServiceTest extends ScheduleTest {
    @InjectMocks
    private AttendeeService attendeeService;

    @Mock
    private TravelAttendeeRepository travelAttendeeRepository;

    @Mock
    private TravelScheduleRepository travelScheduleRepository;

    @Mock
    private MemberRepository memberRepository;

    private TravelSchedule schedule1;
    private Member member1;
    private Member member2;
    private Member member3;
    private TravelAttendee attendee1;
    private TravelAttendee attendee2;

    @BeforeEach
    void setUp(){
        member1 = createMember(1L, "member1");
        member2 = createMember(2L, "member2");
        member3 = createMember(3L, "member3");

        schedule1 = createTravelSchedule(1L, "테스트1");
        TravelSchedule schedule2 = createTravelSchedule(2L, "테스트2");

        attendee1 = createTravelAttendee(member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL);
        attendee2 = createTravelAttendee(member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ);
        TravelAttendee attendee3 = createTravelAttendee(member3, schedule2, AttendeeRole.AUTHOR, AttendeePermission.ALL);
        schedule1.setTravelAttendeeList(new ArrayList<>(List.of(attendee1, attendee2)));
        schedule2.setTravelAttendeeList(new ArrayList<>(List.of(attendee3)));
    }

    @Test
    @DisplayName("createAttendee(): 일정 참석자 추가")
    void createAttendee(){
        // given
        CreateAttendeeRequest createAttendeeRequest = createAttendeeRequest(member3.getUserId(), AttendeePermission.CHAT);

        when(travelScheduleRepository.findByScheduleId(anyLong())).thenReturn(Optional.of(schedule1));
        when(travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_UserIdAndRole(schedule1.getScheduleId(), member1.getUserId(), AttendeeRole.AUTHOR))
                .thenReturn(true);
        when(memberRepository.findByUserId(member3.getUserId())).thenReturn(Optional.of(member3));
        when(travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_UserId(anyLong(), anyString())).thenReturn(false);


        // when, then
        assertDoesNotThrow(() ->  attendeeService.createAttendee(schedule1.getScheduleId(), member1.getUserId(), createAttendeeRequest));

    }

    @Test
    @DisplayName("createAttendee(): 일정 참석자 추가 시 일정 찾을 수 없어 예외 발생")
    void createAttendeeNotFoundSchedule_dataNotFoundException(){
        // given
        CreateAttendeeRequest createAttendeeRequest = createAttendeeRequest(member3.getUserId(), AttendeePermission.CHAT);

        when(travelScheduleRepository.findByScheduleId(anyLong())).thenReturn(Optional.empty());

        // when, then
        DataNotFoundException fail = assertThrows(DataNotFoundException.class, () ->  attendeeService.createAttendee(schedule1.getScheduleId(), member1.getUserId(), createAttendeeRequest));

        assertEquals(fail.getHttpStatus(), ErrorCode.SCHEDULE_NOT_FOUND.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.SCHEDULE_NOT_FOUND.getMessage());

    }

    @Test
    @DisplayName("createAttendee(): 일정 참석자 추가 시 작성자가 아닌 사람의 요청으로 예외 발생")
    void createAttendeeNotAuthor_forbiddenScheduleException(){
        // given
        CreateAttendeeRequest createAttendeeRequest = createAttendeeRequest(member3.getUserId(), AttendeePermission.CHAT);

        when(travelScheduleRepository.findByScheduleId(anyLong())).thenReturn(Optional.of(schedule1));
        when(travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_UserIdAndRole(schedule1.getScheduleId(), member1.getUserId(), AttendeeRole.AUTHOR))
                .thenReturn(false);

        // when, then
        ForbiddenScheduleException fail = assertThrows(ForbiddenScheduleException.class, () ->  attendeeService.createAttendee(schedule1.getScheduleId(), member1.getUserId(), createAttendeeRequest));

        assertEquals(fail.getHttpStatus(), ErrorCode.FORBIDDEN_SHARE_ATTENDEE.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.FORBIDDEN_SHARE_ATTENDEE.getMessage());

    }

    @Test
    @DisplayName("createAttendee(): 일정 참석자 추가 시 참석자 정보를 찾을 수 없어 예외 발생")
    void createAttendeeNotMember_forbiddenScheduleException(){
        // given
        CreateAttendeeRequest createAttendeeRequest = createAttendeeRequest(member3.getUserId(), AttendeePermission.CHAT);

        when(travelScheduleRepository.findByScheduleId(anyLong())).thenReturn(Optional.of(schedule1));
        when(travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_UserIdAndRole(schedule1.getScheduleId(), member1.getUserId(), AttendeeRole.AUTHOR))
                .thenReturn(true);
        when(memberRepository.findByUserId(anyString())).thenReturn(Optional.empty());

        // when, then
        DataNotFoundException fail = assertThrows(DataNotFoundException.class, () ->  attendeeService.createAttendee(schedule1.getScheduleId(), member1.getUserId(), createAttendeeRequest));

        assertEquals(fail.getHttpStatus(), ErrorCode.USER_NOT_FOUND.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.USER_NOT_FOUND.getMessage());

    }

    @Test
    @DisplayName("createAttendee(): 일정 참석자 추가 시 이미 참석자로 존재해 예외 발생")
    void createAttendee_alreadyAttendeeException(){
        // given
        CreateAttendeeRequest createAttendeeRequest = createAttendeeRequest(member3.getUserId(), AttendeePermission.CHAT);

        when(travelScheduleRepository.findByScheduleId(anyLong())).thenReturn(Optional.of(schedule1));
        when(travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_UserIdAndRole(schedule1.getScheduleId(), member1.getUserId(), AttendeeRole.AUTHOR))
                .thenReturn(true);
        when(memberRepository.findByUserId(anyString())).thenReturn(Optional.of(member3));
        when(travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_UserId(anyLong(), anyString())).thenReturn(true);


        // when, then
        AlreadyAttendeeException fail = assertThrows(AlreadyAttendeeException.class, () ->  attendeeService.createAttendee(schedule1.getScheduleId(), member1.getUserId(), createAttendeeRequest));

        assertEquals(fail.getHttpStatus(), ErrorCode.ALREADY_ATTENDEE.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.ALREADY_ATTENDEE.getMessage());

    }



    @Test
    @DisplayName("removeAttendee(): 일정 참석자 제거")
    void removeAttendee(){
        // given
        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_UserId(anyLong(), anyString())).thenReturn(Optional.of(attendee2));

        // when
        attendeeService.removeAttendee(schedule1.getScheduleId(), member2.getUserId());

        // then
        verify(travelAttendeeRepository, times(1)).deleteById(any());
    }

    @Test
    @DisplayName("removeAttendee(): 일정 참석자 제거 시 참가자 정보가 없어 예외 발생")
    void removeAttendeeNoAttendeeData_forbiddenScheduleException(){
        // given
        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_UserId(anyLong(), anyString())).thenReturn(Optional.empty());

        // when
        ForbiddenScheduleException fail = assertThrows(ForbiddenScheduleException.class, () -> attendeeService.removeAttendee(schedule1.getScheduleId(), member1.getUserId()));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE.getMessage());
    }

    @Test
    @DisplayName("removeAttendee(): 일정 참석자 제거 시 사용자가 작성자여서 예외 발생")
    void removeAttendeeIsAuthor_forbiddenScheduleException(){
        // given
        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_UserId(anyLong(), anyString())).thenReturn(Optional.of(attendee1));

        // when
        ForbiddenScheduleException fail = assertThrows(ForbiddenScheduleException.class, () -> attendeeService.removeAttendee(schedule1.getScheduleId(), member1.getUserId()));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.FORBIDDEN_REMOVE_ATTENDEE.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.FORBIDDEN_REMOVE_ATTENDEE.getMessage());
    }

}
