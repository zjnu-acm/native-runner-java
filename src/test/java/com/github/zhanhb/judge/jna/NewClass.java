package com.github.zhanhb.judge.jna;

import com.github.zhanhb.judge.jna.Kernel32.JOBOBJECT_BASIC_LIMIT_INFORMATION;
import java.lang.reflect.Field;
import org.junit.Test;

public class NewClass {

    @Test
    public void test() {
        Field[] declaredFields = JOBOBJECT_BASIC_LIMIT_INFORMATION.class.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            System.out.println('"' + declaredField.getName() + '"' + ',');
        }
    }

}
