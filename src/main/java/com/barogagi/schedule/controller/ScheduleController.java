package com.barogagi.schedule.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "일정", description = "일정 관련 API")
@RestController
@RequestMapping("/schedule")
public class ScheduleController {
}
