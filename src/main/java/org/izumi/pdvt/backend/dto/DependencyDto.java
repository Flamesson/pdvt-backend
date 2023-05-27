package org.izumi.pdvt.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DependencyDto {
    private String artifact;
    private String license;
}
