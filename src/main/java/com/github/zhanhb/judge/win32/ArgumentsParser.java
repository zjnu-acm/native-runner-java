package com.github.zhanhb.judge.win32;

import com.github.zhanhb.judge.jdk.lang.ProcessImpl;

public class ArgumentsParser {

    @SuppressWarnings("ValueOfIncrementOrDecrementUsed")
    public Options parse(String[] args) {
        String inputFileName = null;
        String errFileName = null;
        String outputFileName = null;
        long timeLimit = Long.MAX_VALUE;
        long memoryLimit = Long.MAX_VALUE;
        long outputLimit = Long.MAX_VALUE;
        boolean redirectErrorStream = false;
        String prog = null;

        int i = 0, length = args.length;
        while (i < length) {
            String arg = args[i++];
            if (!arg.isEmpty() && arg.charAt(0) == '-') {
                switch (arg) {
                    case "-input":
                        if (i < length) {
                            inputFileName = args[i++];
                            continue;
                        }
                        break;
                    case "-output":
                        if (i < length) {
                            outputFileName = args[i++];
                            continue;
                        }
                        break;
                    case "-error":
                        if (i < length) {
                            errFileName = args[i++];
                            continue;
                        }
                        break;
                    case "-time":
                    case "-timeLimit":
                        if (i < length) {
                            timeLimit = Long.parseLong(args[i++]);
                            if (timeLimit <= 0) {
                                throw new IllegalStateException("Time limit must great than zero");
                            }
                            continue;
                        }
                        break;
                    case "-memory":
                        if (i < length) {
                            memoryLimit = Long.parseLong(args[i++]);
                            if (memoryLimit <= 0) {
                                throw new IllegalStateException("Memory limit must great than zero");
                            }
                            continue;
                        }
                        break;
                    case "-ol":
                        if (i < length) {
                            outputLimit = Long.parseLong(args[i++]);
                            if (outputLimit <= 0) {
                                throw new IllegalStateException("Output limit must great than zero");
                            }
                            continue;
                        }
                        break;
                    case "-redirectErrorStream":
                        redirectErrorStream = true;
                        continue;
                    default:
                        throw new IllegalStateException("Unknown option '" + arg + "'");
                }
                throw new IllegalStateException(arg + " requires another argument");
            } else {
                --i;
                String[] tmp = new String[args.length - i];
                System.arraycopy(args, i, tmp, 0, tmp.length);
                prog = ProcessImpl.createCommandLine(tmp);
                break;
            }
        }
        if (args.length == 0) {
            throw new IllegalStateException();
        }
        checkNotEmpty(prog, "Program arguments must not be empty");
        checkNotEmpty(inputFileName, "Input file missing");
        checkNotEmpty(outputFileName, "Output file missing");
        if (!redirectErrorStream) {
            checkNotEmpty(outputFileName, "Error output file missing");
        }
        return Options.builder()
                .errFileName(errFileName)
                .inputFileName(inputFileName)
                .redirectErrorStream(redirectErrorStream)
                .memoryLimit(memoryLimit)
                .prog(prog)
                .timeLimit(timeLimit)
                .outputFileName(outputFileName)
                .outputLimit(outputLimit)
                .build();
    }

    private void checkNotEmpty(String test, String message) {
        if (test == null || test.isEmpty()) {
            throw new IllegalStateException(message);
        }
    }

}
