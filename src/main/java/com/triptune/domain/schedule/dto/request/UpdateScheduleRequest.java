package com.triptune.domain.schedule.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class UpdateScheduleRequest {

    @NotBlank(message = "여행지 이름은 필수 입력 값입니다.")
    private String scheduleName;

    @NotNull(message = "여행지 시작 날짜는 필수 입력 값입니다.")
    @FutureOrPresent(message = "오늘 이후 날짜만 입력 가능합니다.")
    private LocalDate startDate;

    @NotNull(message = "여행지 종료 날짜는 필수 입력 값입니다.")
    @FutureOrPresent(message = "오늘 이후 날짜만 입력 가능합니다.")
    private LocalDate endDate;

    private List<RouteRequest> travelRoute = new ArrayList<>();

    @Builder
    public UpdateScheduleRequest(String scheduleName, LocalDate startDate, LocalDate endDate, List<RouteRequest> travelRoute) {
        this.scheduleName = scheduleName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.travelRoute = travelRoute;
    }
}
