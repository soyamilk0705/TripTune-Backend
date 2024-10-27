package com.triptune.domain.schedule.service;

import com.triptune.domain.common.entity.File;
import com.triptune.domain.member.entity.Member;
import com.triptune.domain.member.repository.MemberRepository;
import com.triptune.domain.schedule.dto.*;
import com.triptune.domain.schedule.entity.TravelAttendee;
import com.triptune.domain.schedule.entity.TravelRoute;
import com.triptune.domain.schedule.entity.TravelSchedule;
import com.triptune.domain.schedule.enumclass.AttendeePermission;
import com.triptune.domain.schedule.enumclass.AttendeeRole;
import com.triptune.domain.schedule.repository.TravelAttendeeRepository;
import com.triptune.domain.schedule.repository.TravelRouteRepository;
import com.triptune.domain.schedule.repository.TravelScheduleRepository;
import com.triptune.domain.travel.dto.PlaceResponse;
import com.triptune.domain.travel.entity.TravelImage;
import com.triptune.domain.travel.entity.TravelPlace;
import com.triptune.domain.travel.repository.TravelPlacePlaceRepository;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.exception.DataNotFoundException;
import com.triptune.global.response.pagination.PageResponse;
import com.triptune.global.response.pagination.SchedulePageResponse;
import com.triptune.global.util.PageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final TravelScheduleRepository travelScheduleRepository;
    private final MemberRepository memberRepository;
    private final TravelAttendeeRepository travelAttendeeRepository;
    private final TravelPlacePlaceRepository travelPlaceRepository;
    private final TravelRouteRepository travelRouteRepository;


    public SchedulePageResponse<ScheduleInfoResponse> getSchedules(int page, String userId) {
        Pageable pageable = PageUtil.schedulePageable(page);
        Member member = getSavedMember(userId);

        Page<TravelSchedule> schedulePage = travelScheduleRepository.findTravelSchedulesByAttendee(pageable, member.getMemberId());

        List<ScheduleInfoResponse> scheduleInfoResponseList = new ArrayList<>();
        long sharedScheduleCnt = 0;

        if (!schedulePage.getContent().isEmpty()){
            scheduleInfoResponseList = schedulePage.stream()
                    .map(this::convertToScheduleInfoResponse)
                    .collect(Collectors.toList());

            sharedScheduleCnt = schedulePage.getContent().stream()
                    .filter(schedule -> schedule.getTravelAttendeeList().size() > 1)
                    .count();
        }

        Page<ScheduleInfoResponse> scheduleInfoResponsePage = PageUtil.createPage(scheduleInfoResponseList, pageable, schedulePage.getTotalElements());

        return SchedulePageResponse.of(scheduleInfoResponsePage, sharedScheduleCnt);
    }


    public ScheduleInfoResponse convertToScheduleInfoResponse(TravelSchedule schedule){
        String thumbnailUrl = getThumbnailUrl(schedule);
        AuthorDTO authorDTO = getAuthorDTO(schedule);

        return ScheduleInfoResponse.entityToDto(schedule, thumbnailUrl, authorDTO);
    }

    public AuthorDTO getAuthorDTO(TravelSchedule schedule){
        Member author = getAuthorMember(schedule.getTravelAttendeeList());
        return AuthorDTO.of(author.getUserId(), author.getProfileImage().getS3ObjectUrl());
    }

    public Member getAuthorMember(List<TravelAttendee> attendeeList){
        return attendeeList.stream()
                .filter(attendee -> attendee.getRole().equals(AttendeeRole.AUTHOR))
                .map(TravelAttendee::getMember)
                .findFirst()
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.AUTHOR_NOT_FOUND));
    }


    public String getThumbnailUrl(TravelSchedule schedule){
        List<TravelImage> travelImages = travelRouteRepository.findPlaceImagesOfFirstRoute(schedule.getScheduleId());

        if (!travelImages.isEmpty()){
            return File.getThumbnailUrl(travelImages);
        }
        return null;
    }


    /**
     * 일정 생성
     * @param createScheduleRequest: 일정명, 날짜 등 포함된 dto
     * @param userId: 사용자 아이디
     * @return CreateScheduleResponse: scheduleId로 구성된 dto
     */
    public CreateScheduleResponse createSchedule(CreateScheduleRequest createScheduleRequest, String userId){
        TravelSchedule travelSchedule = TravelSchedule.builder()
                .scheduleName(createScheduleRequest.getScheduleName())
                .startDate(createScheduleRequest.getStartDate())
                .endDate(createScheduleRequest.getEndDate())
                .createdAt(LocalDateTime.now())
                .build();

        TravelSchedule savedTravelSchedule = travelScheduleRepository.save(travelSchedule);

        Member member = getSavedMember(userId);

        TravelAttendee travelAttendee = TravelAttendee.builder()
                .travelSchedule(savedTravelSchedule)
                .member(member)
                .role(AttendeeRole.AUTHOR)
                .permission(AttendeePermission.ALL)
                .build();

        travelAttendeeRepository.save(travelAttendee);

        return CreateScheduleResponse.entityToDto(savedTravelSchedule);
    }

    /**
     * 일정 상세 조회
     * @param scheduleId: 일정 인덱스
     * @param page: 페이지 수
     * @return ScheduleResponse: 일정 정보, 여행지 정보(중구)로 구성된 dto
     */
    public ScheduleDetailResponse getScheduleDetail(Long scheduleId, int page) {
        TravelSchedule schedule = getSavedSchedule(scheduleId);

        // 여행지 정보: Page<TravelPlace> -> PageResponse<TravelSimpleResponse> 로 변경
        Page<PlaceResponse> travelPlacesDTO = getSimpleTravelPlacesByJunggu(page);
        PageResponse<PlaceResponse> placeDTOList = PageResponse.of(travelPlacesDTO);

        List<AttendeeDTO> attendeeDTOList = travelAttendeeRepository.findAllByTravelSchedule_ScheduleId(schedule.getScheduleId())
                .stream()
                .map(AttendeeDTO::entityToDTO)
                .toList();

        return ScheduleDetailResponse.entityToDTO(schedule, placeDTOList, attendeeDTOList);
    }

    /**
     * 여행지 정보 조회
     * @param scheduleId: 일정 인덱스
     * @param page: 페이지 수
     * @return Page<PlaceSimpleResponse>: 중구 기준 여행지 정보로 구성된 페이지 dto
     */
    public Page<PlaceResponse> getTravelPlaces(Long scheduleId, int page) {
        getSavedSchedule(scheduleId);
        return getSimpleTravelPlacesByJunggu(page);
    }

    /**
     * 여행지 검색
     * @param scheduleId: 일정 인덱스
     * @param page: 페이지 수
     * @param keyword: 검색 키워드
     * @return Page<PlaceSimpleResponse>: 여행지 정보로 구성된 페이지 dto
     */
    public Page<PlaceResponse> searchTravelPlaces(Long scheduleId, int page, String keyword) {
        getSavedSchedule(scheduleId);

        Pageable pageable = PageUtil.defaultPageable(page);
        Page<TravelPlace> travelPlaces = travelPlaceRepository.searchTravelPlaces(pageable, keyword);

        return travelPlaces.map(PlaceResponse::entityToDto);
    }

    /**
     * 여행 루트 조회
     * @param scheduleId: 일정 인덱스
     * @param page: 페이지 수
     * @return Page<RouteResponse>: 여행 루트 정보로 구성된 페이지 dto
     */
    public Page<RouteResponse> getTravelRoutes(Long scheduleId, int page) {
        getSavedSchedule(scheduleId);

        Pageable pageable = PageUtil.defaultPageable(page);
        Page<TravelRoute> travelRoutes = travelRouteRepository.findAllByTravelSchedule_ScheduleId(pageable, scheduleId);

        return travelRoutes.map(t -> RouteResponse.entityToDto(t, t.getTravelPlace()));
    }


    /**
     * 중구 여행지 조회
     * @param page: 페이지 수
     * @return Page<PlaceSimpleResponse>: 중구 기준 여행지 정보로 구성된 페이지 dto
     */
    public Page<PlaceResponse> getSimpleTravelPlacesByJunggu(int page) {
        Pageable pageable = PageUtil.defaultPageable(page);
        Page<TravelPlace> travelPlaces = travelPlaceRepository.findAllByAreaData(pageable, "대한민국", "서울", "중구");

        return travelPlaces.map(PlaceResponse::entityToDto);
    }

    /**
     * 저장된 사용자 정보 조회
     * @param userId: 사용자 아이디
     * @return Member: 사용자 entity
     */
    public Member getSavedMember(String userId){
        return memberRepository.findByUserId(userId)
                .orElseThrow(() ->  new DataNotFoundException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * 저장된 일정 조회
     * @param scheduleId: 일정 인덱스
     * @return TravelSchedule: 일정 entity
     */
    public TravelSchedule getSavedSchedule(Long scheduleId){
        return travelScheduleRepository.findByScheduleId(scheduleId)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.DATA_NOT_FOUND));
    }



}


