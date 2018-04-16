package io.github.rxcats.core.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CmdResultCode {
    OK(200),
    INTERNAL_SERVER_ERROR(500);

    final int code;
}
