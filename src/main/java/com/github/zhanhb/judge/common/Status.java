package com.github.zhanhb.judge.common;

public enum Status {

    accepted, presentationError, wrongAnswer, timeLimitExceed,
    memoryLimitExceed, runtimeError, compileError, outputLimitExceed,
    queuing, compiling, running, validating, judgeError, restrictedFunction;

    public boolean isFinalResult() {
        return !name().endsWith("ing"); //NOI18N
    }

    @Override
    public String toString() {
        String s = name().replaceAll("[A-Z]", " $0").trim(); //NOI18N
        return s.substring(0, 1).toUpperCase().concat(s.substring(1));
    }

}
