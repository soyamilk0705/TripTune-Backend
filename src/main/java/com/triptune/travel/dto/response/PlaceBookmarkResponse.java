package com.triptune.travel.dto.response;

import com.triptune.travel.entity.TravelPlace;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PlaceBookmarkResponse {
    private Long placeId;
    private String country;
    private String city;
    private String district;
    private String address;
    private String detailAddress;
    private String placeName;
    private String thumbnailUrl;

    @Builder
    public PlaceBookmarkResponse(Long placeId, String country, String city, String district, String address, String detailAddress, String placeName, String thumbnailUrl) {
        this.placeId = placeId;
        this.country = country;
        this.city = city;
        this.district = district;
        this.address = address;
        this.detailAddress = detailAddress;
        this.placeName = placeName;
        this.thumbnailUrl = thumbnailUrl;
    }

    public static PlaceBookmarkResponse from(TravelPlace travelPlace){
        return PlaceBookmarkResponse.builder()
                .placeId(travelPlace.getPlaceId())
                .country(travelPlace.getCountry().getCountryName())
                .city(travelPlace.getCity().getCityName())
                .district(travelPlace.getDistrict().getDistrictName())
                .address(travelPlace.getAddress())
                .detailAddress(travelPlace.getDetailAddress())
                .placeName(travelPlace.getPlaceName())
                .thumbnailUrl(travelPlace.getThumbnailUrl())
                .build();
    }
}
