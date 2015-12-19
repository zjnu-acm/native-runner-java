package com.github.zhanhb.judge.jna;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import java.io.Closeable;
import java.util.Objects;

public class SafeHandle implements Closeable {

    public static void close(HANDLE handle) {
        if (handle != null && !WinBase.INVALID_HANDLE_VALUE.equals(handle)) {
            Kernel32.INSTANCE.CloseHandle(handle);
        }
    }

    private final HANDLE handle;
    private boolean closed;
    private final Object closeLock = new Object();

    public SafeHandle(HANDLE handle) {
        if (WinBase.INVALID_HANDLE_VALUE.equals(Objects.requireNonNull(handle))) {
            throw new IllegalArgumentException("invalid handle value");
        }
        this.handle = handle;
    }

    public HANDLE get() {
        return handle;
    }

    @Override
    public void close() {
        if (!closed) {
            synchronized (closeLock) {
                if (!closed) {
                    closed = true;
                    close(handle);
                }
            }
        }
    }

    @Override
    public String toString() {
        return String.valueOf(get());
    }

}
