package com.triptune.domain.travel.controller;

import com.triptune.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/travels")
@Tag(name = "Travel Place", description = "여행지 탐색 관련 API")
public class TravelApiController {

    @GetMapping("/list")
    @Operation(summary = "(미완성)여행지 탐색 홈 화면", description = "여행지 탐색 메뉴 클릭 시 나오는 첫 화면")
    public ApiResponse<?> travelPlaceList(){
        return ApiResponse.okResponse();
    }

}