package com.github.zhanhb.judge.jna;

public class Launcher2 {

    public static void main(String[] args) {
        long count = Long.parseLong(args[0]);
        String[] tmp = new String[args.length - 1];
        System.arraycopy(args, 1, tmp, 0, tmp.length);
        for (; count > 0; --count) {
            Launcher.main(tmp);
        }
    }

}
