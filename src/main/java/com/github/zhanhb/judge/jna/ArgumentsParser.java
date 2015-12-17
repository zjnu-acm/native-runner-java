package com.github.zhanhb.judge.jna;

import com.github.zhanhb.judge.jdk.lang.ProcessImpl;

public class ArgumentsParser {

    private String inputFileName;
    private String errFileName;
    private String outputFileName;
    private long timeLimit = Long.MAX_VALUE;
    private long memoryLimit = Long.MAX_VALUE;
    private long outputLimit = Long.MAX_VALUE;
    private boolean redirectErrorStream;
    private String prog;

    @SuppressWarnings("ValueOfIncrementOrDecrementUsed")
    public ArgumentsParser(String[] args) {
        int i = 0, length = args.length;
        while (i < length) {
            String arg = args[i];
            if (!arg.isEmpty() && arg.charAt(0) == '-') {
                switch (arg) {
                    case "-input":
                        if (++i < length) {
                            inputFileName = args[i++];
                            continue;
                        }
                        break;
                    case "-output":
                        if (++i < length) {
                            outputFileName = args[i++];
                            continue;
                        }
                        break;
                    case "-error":
                        if (++i < length) {
                            errFileName = args[i++];
                            continue;
                        }
                        break;
                    case "-time":
                    case "-timeLimit":
                        if (++i < length) {
                            timeLimit = Long.parseLong(args[i++]);
                            if (timeLimit <= 0) {
                                throw new IllegalStateException("Time limit must great than zero");
                            }
                            continue;
                        }
                        break;
                    case "-memory":
                        if (++i < length) {
                            memoryLimit = Long.parseLong(args[i++]);
                            if (memoryLimit <= 0) {
                                throw new IllegalStateException("Memory limit must great than zero");
                            }
                            continue;
                        }
                        break;
                    case "-ol":
                        if (++i < length) {
                            outputLimit = Long.parseLong(args[i++]);
                            if (outputLimit <= 0) {
                                throw new IllegalStateException("Output limit must great than zero");
                            }
                            continue;
                        }
                        break;
                    case "-redirectErrorStream":
                        redirectErrorStream = true;
                        ++i;
                        continue;
                    default:
                        throw new IllegalStateException("Unknown option '" + arg + "'");
                }
                throw new IllegalStateException("Never goes here");
            } else {
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
    }

    private void checkNotEmpty(String test, String message) {
        if (test == null || test.isEmpty()) {
            throw new IllegalStateException(message);
        }
    }

    public String getInputFileName() {
        return inputFileName;
    }

    public String getErrFileName() {
        return errFileName;
    }

    public String getOutputFileName() {
        return outputFileName;
    }

    public long getTimeLimit() {
        return timeLimit;
    }

    public long getMemoryLimit() {
        return memoryLimit;
    }

    public long getOutputLimit() {
        return outputLimit;
    }

    public boolean isRedirectErrorStream() {
        return redirectErrorStream;
    }

    public String getProg() {
        return prog;
    }

}
