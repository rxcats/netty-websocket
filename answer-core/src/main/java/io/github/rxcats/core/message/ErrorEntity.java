package io.github.rxcats.core.message;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ErrorEntity {
    private String message;
}
