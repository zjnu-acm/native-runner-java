package com.github.zhanhb.judge.jna;

import static com.github.zhanhb.judge.jna.Constants.*;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinBase;
import static com.sun.jna.platform.win32.WinBase.*;
import com.sun.jna.platform.win32.WinBase.PROCESS_INFORMATION;
import com.sun.jna.platform.win32.WinDef;
import static com.sun.jna.platform.win32.WinNT.*;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import java.io.File;

public class Main {

    public static final int _O_RDONLY = 0;
    public static final int _O_WRONLY = 1;
    public static final int _O_RDWR = 2;

    /* Mask for access mode bits in the _open flags. */
    public static final int _O_ACCMODE = _O_WRONLY | _O_RDWR;

    public static final int _O_APPEND = 0x0008;
    /* Writes will add to the end of the file. */

    public static final int _O_RANDOM = 0x0010;
    public static final int _O_SEQUENTIAL = 0x0020;
    public static final int _O_TEMPORARY = 0x0040;
    /* Make the file dissappear after closing.
				 * WARNING: Even if not created by _open! */
    public static final int _O_NOINHERIT = 0x0080;

    public static final int _O_CREAT = 0x0100;
    /* Create the file if it does not exist. */
    public static final int _O_TRUNC = 0x0200;
    /* Truncate the file if it does exist. */
    public static final int _O_EXCL = 0x0400;
    /* Open only if the file does not exist. */

    public static final int _O_SHORT_LIVED = 0x1000;

    /* NOTE: Text is the default even if the given _O_TEXT bit is not on. */
    public static final int _O_TEXT = 0x4000;
    /* CR-LF in file becomes LF in memory. */
    public static final int _O_BINARY = 0x8000;
    /* Input and output is not translated. */
    public static final int _O_RAW = _O_BINARY;

    public static final int _O_WTEXT = 0x10000;
    public static final int _O_U16TEXT = 0x20000;
    public static final int _O_U8TEXT = 0x40000;

    /* POSIX/Non-ANSI names for increased portability */
    public static final int O_RDONLY = _O_RDONLY;
    public static final int O_WRONLY = _O_WRONLY;
    public static final int O_RDWR = _O_RDWR;
    public static final int O_ACCMODE = _O_ACCMODE;
    public static final int O_APPEND = _O_APPEND;
    public static final int O_CREAT = _O_CREAT;
    public static final int O_TRUNC = _O_TRUNC;
    public static final int O_EXCL = _O_EXCL;
    public static final int O_TEXT = _O_TEXT;
    public static final int O_BINARY = _O_BINARY;
    public static final int O_TEMPORARY = _O_TEMPORARY;
    public static final int O_NOINHERIT = _O_NOINHERIT;
    public static final int O_SEQUENTIAL = _O_SEQUENTIAL;
    public static final int O_RANDOM = _O_RANDOM;
    public static final int O_SYNC = 0x0800;
    public static final int O_DSYNC = 0x2000;
    private static final Sandbox sandbox = new Sandbox();
    private static final Object finalizer = new Object() {

        @Override
        @SuppressWarnings("FinalizeDeclaration")
        protected void finalize() throws Throwable {
            try {
                sandbox.close();
            } finally {
                super.finalize();
            }
        }
    };

    private static HANDLE fileOpen(String path, int flags) {
        Kernel32 kernel32 = Kernel32.INSTANCE;
        int access
                = (flags & O_WRONLY) != 0 ? GENERIC_WRITE
                        : (flags & O_RDWR) != 0 ? (GENERIC_READ | GENERIC_WRITE)
                                : GENERIC_READ;
        int sharing = FILE_SHARE_READ | FILE_SHARE_WRITE;
        /* Note: O_TRUNC overrides O_CREAT */
        int disposition
                = (flags & O_TRUNC) != 0 ? CREATE_ALWAYS
                        : (flags & O_CREAT) != 0 ? OPEN_ALWAYS
                                : OPEN_EXISTING;
        int maybeWriteThrough
                = (flags & (O_SYNC | O_DSYNC)) != 0
                        ? FILE_FLAG_WRITE_THROUGH
                        : FILE_ATTRIBUTE_NORMAL;
        int maybeDeleteOnClose
                = (flags & O_TEMPORARY) != 0
                        ? FILE_FLAG_DELETE_ON_CLOSE
                        : FILE_ATTRIBUTE_NORMAL;

        int flagsAndAttributes = maybeWriteThrough | maybeDeleteOnClose;
        HANDLE h = kernel32.CreateFile(
                path, /* Wide char path name */
                access, /* Read and/or write permission */
                sharing, /* File sharing flags */
                null, /* Security attributes */
                disposition, /* creation disposition */
                flagsAndAttributes, /* flags and attributes */
                null);

        if (h == null || INVALID_HANDLE_VALUE.equals(h)) {
            throw new Win32Exception(kernel32.GetLastError());
        }
        return h;
    }

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public static void main(String[] args) {
        Kernel32 kernel32 = Kernel32.INSTANCE;
        ArgumentsParser parser = new ArgumentsParser(args);

        String prog = parser.getProg();
        String inputFileName = parser.getInputFileName();
        String outputFileName = parser.getOutputFileName();
        boolean redirectErrorStream = parser.isRedirectErrorStream();
        String errFileName = parser.getErrFileName();

        long timeLimit = parser.getTimeLimit();
        long memoryLimit = parser.getMemoryLimit();
        long outputLimit = parser.getOutputLimit();

        try (CloseableHandle hIn = new CloseableHandle(fileOpen(inputFileName, O_RDONLY));
                CloseableHandle hOut = new CloseableHandle(fileOpen(outputFileName, O_WRONLY | O_CREAT | O_TRUNC))) {
            CloseableHandle hErr = redirectErrorStream ? hOut : new CloseableHandle(fileOpen(errFileName, O_WRONLY | O_CREAT | O_TRUNC));

            PROCESS_INFORMATION pi = createProcess(prog, hIn.get(), hOut.get(), hErr.get(), redirectErrorStream);

            try (CloseableHandle hProcess = new CloseableHandle(pi.hProcess);
                    CloseableHandle hThread = new CloseableHandle(pi.hThread)) {
                Judgement judgement = new Judgement(hProcess.get());

                sandbox.beforeProcessStart(hProcess.get());

                while (true) {
                    int dwCount = Kernel32Ex.INSTANCE.ResumeThread(hThread.get());
                    if (dwCount == -1) {
                        throw new Win32Exception(kernel32.GetLastError());
                    }
                    if (dwCount == 0) {
                        break;
                    }
                }

                hThread.close();

                while (true) {
                    long memory = judgement.getMemory();
                    if (memory > memoryLimit) {
                        judgement.terminate(MEMORY_LIMIT_EXCEED);
                        break;
                    }
                    long time = System.currentTimeMillis() - judgement.getStartTime();
                    if (timeLimit <= time) {
                        judgement.terminate(TIME_LIMIT_EXCEED);
                        judgement.join(TERMINATE_TIMEOUT);
                        break;
                    }
                    long dwWaitTime = timeLimit - time;
                    if (dwWaitTime > UPDATE_TIME_THRESHOLD) {
                        dwWaitTime = UPDATE_TIME_THRESHOLD;
                    }
                    if (judgement.join(dwWaitTime)) {
                        break;
                    }
                    if (new File(outputFileName).length() > outputLimit) {
                        judgement.terminate(OUTPUT_LIMIT_EXCEED);
                    }
                    if (!redirectErrorStream && new File(errFileName).length() > outputLimit) {
                        judgement.terminate(OUTPUT_LIMIT_EXCEED);
                    }
                }
                System.out.printf("{time:%d,memory:%d,haltCode:%d,exitCode:%s}%n",
                        judgement.getTime(),
                        judgement.getMemory(),
                        judgement.getHaltCode(),
                        Long.toString(judgement.getExitCode() & 0xffffffffL));
            }
        }

    }

    private static PROCESS_INFORMATION createProcess(String lpCommandLine, HANDLE hIn, HANDLE hOut, HANDLE hErr,
            boolean redirectErrorStream) {
        Kernel32 kernel32 = Kernel32.INSTANCE;

        WinBase.SECURITY_ATTRIBUTES sa = new WinBase.SECURITY_ATTRIBUTES();
        sa.bInheritHandle = true;

        String lpApplicationName = null;
        WinBase.SECURITY_ATTRIBUTES lpProcessAttributes = null;
        WinBase.SECURITY_ATTRIBUTES lpThreadAttributes = null;
        WinDef.DWORD dwCreationFlags = new WinDef.DWORD(CREATE_NO_WINDOW | CREATE_UNICODE_ENVIRONMENT | CREATE_SUSPENDED);
        String lpCurrentDirectory = null;
        WinBase.STARTUPINFO lpStartupInfo = new WinBase.STARTUPINFO();
        WinBase.PROCESS_INFORMATION lpProcessInformation = new WinBase.PROCESS_INFORMATION();

        lpStartupInfo.dwFlags = STARTF_USESTDHANDLES;
        lpStartupInfo.hStdInput = hIn;
        lpStartupInfo.hStdOutput = hOut;
        lpStartupInfo.hStdError = hErr;

        kernel32.SetHandleInformation(hIn, HANDLE_FLAG_INHERIT, 1);
        kernel32.SetHandleInformation(hOut, HANDLE_FLAG_INHERIT, 1);
        if (!redirectErrorStream) {
            kernel32.SetHandleInformation(hErr, HANDLE_FLAG_INHERIT, 1);
        }

        Kernel32Ex kernel32Ex = Kernel32Ex.INSTANCE;
        // pass error mode SEM_NOGPFAULTERRORBOX to the child process
        int oldErrorMode = kernel32Ex.SetErrorMode(Kernel32Ex.SEM_NOGPFAULTERRORBOX);
        try {
            if (!kernel32.CreateProcess(
                    lpApplicationName, // executable name
                    lpCommandLine,// command line
                    lpProcessAttributes, // process security attribute
                    lpThreadAttributes, // thread security attribute
                    true, // inherits system handles
                    dwCreationFlags, // selected based on exe type
                    null,
                    lpCurrentDirectory,
                    lpStartupInfo,
                    lpProcessInformation)) {
                throw new Win32Exception(kernel32.GetLastError());
            }
        } finally {
            kernel32Ex.SetErrorMode(oldErrorMode);
        }
        return lpProcessInformation;

    }

}
