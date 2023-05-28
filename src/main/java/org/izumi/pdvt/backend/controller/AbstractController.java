package org.izumi.pdvt.backend.controller;

import io.jmix.core.CorsProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public abstract class AbstractController {
    protected CorsProperties corsProperties;

    @Autowired
    protected void setCorsProperties(CorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }

    protected  <T> ResponseEntity<T> ok() {
        return ResponseEntity.ok()
                .build();
    }

    protected  <T> ResponseEntity<T> ok(T body) {
        return ResponseEntity.ok()
                .body(body);
    }

    protected <T> ResponseEntity<T> badRequest() {
        return ResponseEntity.badRequest()
                .build();
    }

    protected <T> ResponseEntity<T> forbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .build();
    }
}
