package com.github.zhanhb.judge.jna;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;

@Builder
@Getter
@Value
@SuppressWarnings("FinalClass")
public class Options {

    private String inputFileName;
    private String errFileName;
    private String outputFileName;
    private long timeLimit;
    private long memoryLimit;
    private long outputLimit;
    private boolean redirectErrorStream;
    private String prog;

}
