package com.github.zhanhb.judge.jna;

import com.github.zhanhb.judge.common.ExecuteResult;

public class Launcher {

    public static void main(String[] args) {
        Options options = new ArgumentsParser().parse(args);
        Executor executor = new Executor();
        ExecuteResult result = executor.execute(options);
        System.out.printf("{time:%d,memory:%d,haltCode:%d,exitCode:%s}%n",
                result.getTime(),
                result.getMemory(),
                result.getHaltCode() != null ? result.getHaltCode().ordinal() : null,
                Long.toString(result.getExitCode() & 0xffffffffL));
    }

}
