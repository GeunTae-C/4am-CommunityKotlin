package com.example.community_4am_kotlin.feature.article.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class MainController {
    @GetMapping("/")
    fun index():String{return "/main/mainPage1"}
}