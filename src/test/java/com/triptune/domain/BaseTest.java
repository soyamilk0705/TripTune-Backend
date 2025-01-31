package com.triptune.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.triptune.domain.bookmark.entity.Bookmark;
import com.triptune.domain.common.entity.*;
import com.triptune.domain.member.entity.Member;
import com.triptune.domain.profile.entity.ProfileImage;
import com.triptune.domain.schedule.entity.ChatMessage;
import com.triptune.domain.schedule.entity.TravelAttendee;
import com.triptune.domain.schedule.entity.TravelRoute;
import com.triptune.domain.schedule.entity.TravelSchedule;
import com.triptune.domain.schedule.enumclass.AttendeePermission;
import com.triptune.domain.schedule.enumclass.AttendeeRole;
import com.triptune.domain.travel.entity.TravelImage;
import com.triptune.domain.travel.entity.TravelPlace;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Transactional
public abstract class BaseTest {

    @Autowired
    private ObjectMapper objectMapper;

    private final String refreshToken = "MemberRefreshToken";


    protected Member createMember(Long memberId, String userId){
        return Member.builder()
                .memberId(memberId)
                .userId(userId)
                .email(userId + "@email.com")
                .password("test123@")
                .nickname(userId)
                .refreshToken(refreshToken)
                .isSocialLogin(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    protected Member createMember(Long memberId, String userId, ProfileImage profileImage){
        return Member.builder()
                .memberId(memberId)
                .userId(userId)
                .email(userId + "@email.com")
                .password("test123@")
                .nickname(userId)
                .refreshToken(refreshToken)
                .isSocialLogin(false)
                .createdAt(LocalDateTime.now())
                .profileImage(profileImage)
                .build();
    }

    protected Member createMember(Long memberId, String userId, String encodePassword){
        return Member.builder()
                .memberId(memberId)
                .userId(userId)
                .email(userId + "@email.com")
                .password(encodePassword)
                .nickname(userId)
                .refreshToken(refreshToken)
                .isSocialLogin(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    protected ProfileImage createProfileImage(Long profileImageId, String fileName){
        return ProfileImage.builder()
                .profileImageId(profileImageId)
                .s3ObjectUrl("/test/" + fileName + ".jpg")
                .originalName(fileName + "_original.jpg")
                .fileName(fileName + ".jpg")
                .fileType("jpg")
                .fileSize(20)
                .createdAt(LocalDateTime.now())
                .build();
    }

    protected Country createCountry(){
        return Country.builder().countryName("대한민국").build();
    }

    protected City createCity(Country country){
        return City.builder().cityName("서울").country(country).build();
    }

    protected District createDistrict(City city, String districtName){
        return District.builder().districtName(districtName).city(city).build();
    }

    protected ApiCategory createApiCategory(){
        return ApiCategory.builder().categoryCode("A0101").categoryName("자연").level(1).build();
    }

    protected TravelPlace createTravelPlace(Long placeId, Country country, City city, District district, ApiCategory apiCategory){
        return TravelPlace.builder()
                .placeId(placeId)
                .country(country)
                .city(city)
                .district(district)
                .apiCategory(apiCategory)
                .address("테스트 주소")
                .detailAddress("테스트 상세주소")
                .homepage("www.test.com")
                .phoneNumber("010-0000-0000")
                .latitude(37.5)
                .longitude(127.0281573537)
                .placeName("테스트 장소명")
                .bookmarkCnt(0)
                .createdAt(LocalDateTime.now())
                .description("테스트 장소 설명")
                .build();
    }

    protected TravelPlace createTravelPlace(Long placeId, Country country, City city, District district, ApiCategory apiCategory, double latitude, double longitude){
        return TravelPlace.builder()
                .placeId(placeId)
                .country(country)
                .city(city)
                .district(district)
                .apiCategory(apiCategory)
                .address("테스트 주소")
                .detailAddress("테스트 상세주소")
                .homepage("www.test.com")
                .phoneNumber("010-0000-0000")
                .latitude(latitude)
                .longitude(longitude)
                .placeName("테스트 장소명")
                .bookmarkCnt(0)
                .createdAt(LocalDateTime.now())
                .description("테스트 장소 설명")
                .build();
    }

    protected TravelSchedule createTravelSchedule(Long scheduleId, String scheduleName){
        return TravelSchedule.builder()
                .scheduleId(scheduleId)
                .scheduleName(scheduleName)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(10))
                .createdAt(LocalDateTime.now())
                .build();
    }



    protected TravelImage createTravelImage(TravelPlace travelPlace, String fileName, boolean isThumbnail){
        return TravelImage.builder()
                .travelPlace(travelPlace)
                .s3ObjectUrl("/test/" + fileName + ".jpg")
                .originalName(fileName + "_original.jpg")
                .fileName(fileName + ".jpg")
                .fileType("jpg")
                .fileSize(20)
                .createdAt(LocalDateTime.now())
                .isThumbnail(isThumbnail)
                .build();
    }


    protected ApiContentType createApiContentType(String contentTypeName){
        return ApiContentType.builder()
                .contentTypeName(contentTypeName)
                .build();
    }

    protected TravelAttendee createTravelAttendee(Long attendeeId, Member member, TravelSchedule schedule, AttendeeRole role, AttendeePermission permission){
        return TravelAttendee.builder()
                .attendeeId(attendeeId)
                .member(member)
                .travelSchedule(schedule)
                .role(role)
                .permission(permission)
                .build();
    }

    protected TravelRoute createTravelRoute(TravelSchedule schedule, TravelPlace travelPlace, int routeOrder){
        return TravelRoute.builder()
                .travelSchedule(schedule)
                .travelPlace(travelPlace)
                .routeOrder(routeOrder)
                .build();
    }

    protected ChatMessage createChatMessage(String messageId, Long scheduleId, Member member, String message){
        return ChatMessage.builder()
                .messageId(messageId)
                .scheduleId(scheduleId)
                .memberId(member.getMemberId())
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    protected Bookmark createBookmark(Long bookmarkId, Member member, TravelPlace travelPlace){
        return Bookmark.builder()
                .bookmarkId(bookmarkId)
                .member(member)
                .travelPlace(travelPlace)
                .createdAt(LocalDateTime.now())
                .build();
    }


    protected String toJsonString(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }
}
