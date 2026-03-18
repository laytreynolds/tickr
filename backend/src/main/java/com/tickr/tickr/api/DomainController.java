package com.tickr.tickr.api;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

@RestController
@RequiredArgsConstructor
public class DomainController {

    @GetMapping("/tickr/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}