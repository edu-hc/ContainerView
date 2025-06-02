package com.ftc.containerView.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/error")
public class ErrorController {

    private static final Logger logger = LoggerFactory.getLogger(ErrorController.class);

    @GetMapping
    public ResponseEntity<String> handleError() {
        logger.warn("Acesso negado ao endpoint /error.");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso Negado");
    }
}
