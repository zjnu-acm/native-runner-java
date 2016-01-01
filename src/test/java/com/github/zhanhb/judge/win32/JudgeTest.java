package com.github.zhanhb.judge.win32;

import com.github.zhanhb.judge.win32.Options;
import com.github.zhanhb.judge.win32.Executor;
import com.github.zhanhb.judge.win32.ArgumentsParser;
import com.github.zhanhb.judge.common.ExecuteResult;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import lombok.Builder;
import lombok.Value;

/**
 *
 * @author zhanhb
 */
public class JudgeTest {

    public static void main(String[] args) throws IOException, URISyntaxException {
        final Path path = Paths.get("src/test/resources/sample/program");
        final ArrayList<TestCase> list = new ArrayList<>(20);
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                list.add(TestCase.builder()
                        .file(file)
                        .type(path.relativize(file.getParent()).toString())
                        .build());
                return super.visitFile(file, attrs);
            }
        });
        ArrayList<File> cp = new ArrayList<>(40);
        for (ClassLoader cl = JudgeTest.class.getClassLoader(); cl != null; cl = cl.getParent()) {
            if (cl instanceof URLClassLoader) {
                for (URL url : ((URLClassLoader) cl).getURLs()) {
                    cp.add(new File(url.toURI()));
                }
            }
        }
        StringBuilder sb = new StringBuilder(400);
        for (File file : cp) {
            sb.append(file).append(File.pathSeparatorChar);
        }
        sb.setLength(sb.length() - 1);
        String classpath = sb.toString();
        ArgumentsParser parser = new ArgumentsParser();
        Executor executor = new Executor();

        for (TestCase testCase : list) {
            Options options = parser.parse(new String[]{
                "-input",
                "src/test/resources/sample/data/a.in",
                "-output",
                "target/test/out.txt",
                "-error",
                getNull(),
                "-time",
                "3000",
                "-memory",
                Long.toString(256 * 1024 * 1024), // 64M
                "-ol",
                "16777216",
                "java",
                "-cp",
                classpath,
                groovy.ui.GroovyMain.class.getName(),
                testCase.getFile().toRealPath().toString()
            });
            ExecuteResult executeResult = executor.execute(options);
            System.out.println(executeResult + " " + testCase);
        }

    }

    private static String getNull() {
        return File.separatorChar == '\\' ? "nul" : "/dev/null";
    }

    @Builder
    @Value
    @SuppressWarnings("FinalClass")
    private static class TestCase {

        private String type;
        private Path file;

    }

}
