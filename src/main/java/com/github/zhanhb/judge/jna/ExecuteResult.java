package com.github.zhanhb.judge.jna;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;

@Builder
@Getter
@Value
public class ExecuteResult {

    private long time;
    private long memory;
    private int haltCode;
    private int exitCode;

}
