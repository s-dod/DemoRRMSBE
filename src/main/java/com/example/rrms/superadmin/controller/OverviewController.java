package com.example.rrms.superadmin.controller;

import com.example.rrms.superadmin.DTO.response.OverviewActivityResponse;
import com.example.rrms.superadmin.DTO.response.OverviewStatsResponse;
import com.example.rrms.superadmin.service.OverviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/overview")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class OverviewController {

    private final OverviewService overviewService;

    @GetMapping("/stats")
    public OverviewStatsResponse getStats() {
        return overviewService.getStats();
    }

    @GetMapping("/activity")
    public OverviewActivityResponse getActivity() {
        return overviewService.getActivity();
    }
}
