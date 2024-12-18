package com.triptune.domain.schedule.service;

import com.triptune.domain.schedule.dto.response.RouteResponse;
import com.triptune.domain.schedule.entity.TravelRoute;
import com.triptune.domain.schedule.repository.TravelRouteRepository;
import com.triptune.domain.schedule.repository.TravelScheduleRepository;
import com.triptune.global.util.PageUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class RouteService {

    private final TravelRouteRepository travelRouteRepository;

    public Page<RouteResponse> getTravelRoutes(Long scheduleId, int page) {
        Pageable pageable = PageUtil.defaultPageable(page);
        Page<TravelRoute> travelRoutes = travelRouteRepository.findAllByTravelSchedule_ScheduleId(pageable, scheduleId);

        return travelRoutes.map(RouteResponse::from);
    }
}
