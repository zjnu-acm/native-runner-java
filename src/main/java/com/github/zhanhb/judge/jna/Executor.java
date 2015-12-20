package com.github.zhanhb.judge.jna;

import com.github.zhanhb.judge.jna.Advapi32.SID_AND_ATTRIBUTES;
import com.github.zhanhb.judge.jna.Advapi32.SID_IDENTIFIER_AUTHORITY;
import com.github.zhanhb.judge.jna.Advapi32.TOKEN_MANDATORY_LABEL;
import static com.github.zhanhb.judge.jna.Constants.*;
import static com.github.zhanhb.judge.jna.Kernel32.*;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinBase;
import static com.sun.jna.platform.win32.WinBase.CREATE_BREAKAWAY_FROM_JOB;
import static com.sun.jna.platform.win32.WinBase.CREATE_NEW_PROCESS_GROUP;
import static com.sun.jna.platform.win32.WinBase.CREATE_NO_WINDOW;
import static com.sun.jna.platform.win32.WinBase.CREATE_SUSPENDED;
import static com.sun.jna.platform.win32.WinBase.CREATE_UNICODE_ENVIRONMENT;
import static com.sun.jna.platform.win32.WinBase.HANDLE_FLAG_INHERIT;
import static com.sun.jna.platform.win32.WinBase.INVALID_HANDLE_VALUE;
import com.sun.jna.platform.win32.WinBase.PROCESS_INFORMATION;
import static com.sun.jna.platform.win32.WinBase.STARTF_USESTDHANDLES;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import static com.sun.jna.platform.win32.WinNT.CREATE_ALWAYS;
import static com.sun.jna.platform.win32.WinNT.FILE_ATTRIBUTE_NORMAL;
import static com.sun.jna.platform.win32.WinNT.FILE_FLAG_DELETE_ON_CLOSE;
import static com.sun.jna.platform.win32.WinNT.FILE_FLAG_WRITE_THROUGH;
import static com.sun.jna.platform.win32.WinNT.FILE_SHARE_READ;
import static com.sun.jna.platform.win32.WinNT.FILE_SHARE_WRITE;
import static com.sun.jna.platform.win32.WinNT.GENERIC_READ;
import static com.sun.jna.platform.win32.WinNT.GENERIC_WRITE;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.WinNT.HANDLEByReference;
import static com.sun.jna.platform.win32.WinNT.OPEN_ALWAYS;
import static com.sun.jna.platform.win32.WinNT.OPEN_EXISTING;
import static com.sun.jna.platform.win32.WinNT.TOKEN_ADJUST_DEFAULT;
import static com.sun.jna.platform.win32.WinNT.TOKEN_ASSIGN_PRIMARY;
import static com.sun.jna.platform.win32.WinNT.TOKEN_DUPLICATE;
import com.sun.jna.platform.win32.WinNT.TOKEN_INFORMATION_CLASS;
import static com.sun.jna.platform.win32.WinNT.TOKEN_QUERY;
import java.io.File;

public class Executor {

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

    private HANDLE fileOpen(String path, int flags) {
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

    public ExecuteResult execute(Options options) {
        Kernel32 kernel32 = Kernel32.INSTANCE;

        String prog = options.getProg();
        String inputFileName = options.getInputFileName();
        String outputFileName = options.getOutputFileName();
        boolean redirectErrorStream = options.isRedirectErrorStream();
        String errFileName = options.getErrFileName();

        long timeLimit = options.getTimeLimit();
        long memoryLimit = options.getMemoryLimit();
        long outputLimit = options.getOutputLimit();

        try (Sandbox sandbox = new Sandbox();
                SafeHandle hIn = new SafeHandle(fileOpen(inputFileName, O_RDONLY));
                SafeHandle hOut = new SafeHandle(fileOpen(outputFileName, O_WRONLY | O_CREAT | O_TRUNC))) {
            SafeHandle hErr = redirectErrorStream ? hOut : new SafeHandle(fileOpen(errFileName, O_WRONLY | O_CREAT | O_TRUNC));

            PROCESS_INFORMATION pi = createProcess(prog, hIn.get(), hOut.get(), hErr.get(), redirectErrorStream);

            try (SafeHandle hProcess = new SafeHandle(pi.hProcess);
                    SafeHandle hThread = new SafeHandle(pi.hThread)) {
                Judgement judgement = new Judgement(hProcess.get());

                sandbox.beforeProcessStart(hProcess.get());

                while (true) {
                    int dwCount = Kernel32.INSTANCE.ResumeThread(hThread.get());
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
                return ExecuteResult.builder()
                        .time(judgement.getTime())
                        .memory(judgement.getMemory())
                        .haltCode(judgement.getHaltCode())
                        .exitCode(judgement.getExitCode())
                        .build();
            }
        }

    }

    private PROCESS_INFORMATION createProcess(String lpCommandLine, HANDLE hIn, HANDLE hOut, HANDLE hErr,
            boolean redirectErrorStream) {
        Kernel32 kernel32 = Kernel32.INSTANCE;

        WinBase.SECURITY_ATTRIBUTES sa = new WinBase.SECURITY_ATTRIBUTES();
        sa.bInheritHandle = true;

        String lpApplicationName = null;
        WinBase.SECURITY_ATTRIBUTES lpProcessAttributes = new WinBase.SECURITY_ATTRIBUTES();
        WinBase.SECURITY_ATTRIBUTES lpThreadAttributes = new WinBase.SECURITY_ATTRIBUTES();
        WinDef.DWORD dwCreationFlags = new WinDef.DWORD(
                CREATE_SUSPENDED
                | HIGH_PRIORITY_CLASS
                | CREATE_NEW_PROCESS_GROUP
                | CREATE_UNICODE_ENVIRONMENT
                | CREATE_BREAKAWAY_FROM_JOB
                | CREATE_NO_WINDOW
        );
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

        try (SafeHandle hToken = new SafeHandle(createRestrictedToken())) {
            SID_IDENTIFIER_AUTHORITY pIdentifierAuthority = new SID_IDENTIFIER_AUTHORITY(new byte[]{0, 0, 0, 0, 0, 16});

            int SECURITY_MANDATORY_LOW_RID = 0x1000;
            int SE_GROUP_INTEGRITY = 0x00000020;

            WinNT.PSID pSid = Advapi32Util.newPSID(pIdentifierAuthority, SECURITY_MANDATORY_LOW_RID);

            TOKEN_MANDATORY_LABEL TokenInformation = new TOKEN_MANDATORY_LABEL();
            TokenInformation.Label = new SID_AND_ATTRIBUTES();
            TokenInformation.Label.Attributes = SE_GROUP_INTEGRITY;
            TokenInformation.Label.Sid = pSid.getPointer();

            if (!Advapi32.INSTANCE.SetTokenInformation(
                    hToken.get(),
                    TOKEN_INFORMATION_CLASS.TokenIntegrityLevel,
                    TokenInformation,
                    TokenInformation.size() + Advapi32.INSTANCE.GetLengthSid(pSid))) {
                throw new Win32Exception(kernel32.GetLastError());
            }

            // pass error mode SEM_NOGPFAULTERRORBOX to the child process
            int oldErrorMode = kernel32.SetErrorMode(Kernel32.SEM_NOGPFAULTERRORBOX);
            try {
                if (!kernel32.CreateProcessAsUser(
                        hToken.get(),
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
                kernel32.SetErrorMode(oldErrorMode);
            }
            return lpProcessInformation;
        }

    }

    private HANDLE createRestrictedToken() {
        Kernel32 kernel32 = Kernel32.INSTANCE;
        WinNT.HANDLEByReference TokenHandle = new HANDLEByReference();
        if (!kernel32.OpenProcessToken(kernel32.GetCurrentProcess(),
                TOKEN_DUPLICATE | TOKEN_ASSIGN_PRIMARY | TOKEN_QUERY | TOKEN_ADJUST_DEFAULT,
                TokenHandle)) {
            throw new Win32Exception(kernel32.GetLastError());
        }

        int SANDBOX_INERT = 2;
        HANDLEByReference NewTokenHandle = new HANDLEByReference();
        try {
            if (!Advapi32.INSTANCE.CreateRestrictedToken(
                    TokenHandle.getValue(), // ExistingTokenHandle
                    SANDBOX_INERT, // Flags
                    0, // DisableSidCount
                    null, // SidsToDisable
                    0, // DeletePrivilegeCount
                    null, // PrivilegesToDelete
                    0, // RestrictedSidCount
                    null, // SidsToRestrict
                    NewTokenHandle // NewTokenHandle
            )) {
                throw new Win32Exception(kernel32.GetLastError());
            }

        } finally {
            SafeHandle.close(TokenHandle.getValue());
        }
        return NewTokenHandle.getValue();
    }

}
