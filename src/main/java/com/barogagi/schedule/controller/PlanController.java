package com.barogagi.schedule.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "계획", description = "계획 관련 API")
@RestController
@RequestMapping("/plan")
public class PlanController {
}
