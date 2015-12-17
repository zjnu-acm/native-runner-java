package com.github.zhanhb.judge.jna;

public interface Constants {

    int ACCEPTED = 0x00000000;
    int PRESENTATION_ERROR = 0x00000001;
    int WRONG_ANSWER = 0x00000002;
    int TIME_LIMIT_EXCEED = 0x00000003;
    int MEMORY_LIMIT_EXCEED = 0x00000004;
    int RUNTIME_ERROR = 0x00000005;
    int COMPILE_ERROR = 0x00000006;
    int OUTPUT_LIMIT_EXCEED = 0x00000007;
    int QUEUING = 0x00000008;
    int COMPILING = 0x00000009;
    int RUNNING = 0x0000000A;
    int VALIDATING = 0x0000000B;
    int JUDGE_ERROR = 0x0000000C;
    int RESTRICTED_FUNCTION = 0x0000000D;
    int TERMINATE_TIMEOUT = 40;

    int UPDATE_TIME_THRESHOLD = 50;

}
