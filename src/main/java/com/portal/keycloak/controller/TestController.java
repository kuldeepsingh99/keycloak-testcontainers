package com.portal.keycloak.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2")
public class TestController {


    @GetMapping("/customers")
    public String getCustomers() {
        return "Hello Customers";
    }

    @PreAuthorize("hasAuthority('products:read')")
    @GetMapping("/products")
    public String getProducts() {
        return "Hello Products";
    }
}
