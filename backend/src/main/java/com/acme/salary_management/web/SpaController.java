package com.acme.salary_management.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Angular handles these routes in the browser; on a page refresh the server must return index.html. */
@Controller
public class SpaController {

    @GetMapping({"/employees", "/insights"})
    public String forwardToAngular() {
        return "forward:/index.html";
    }
}