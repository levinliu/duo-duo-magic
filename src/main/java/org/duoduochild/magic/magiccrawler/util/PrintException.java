package org.duoduochild.magic.magiccrawler.util;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by levinliu on 2017/10/11
 * GitHub: https://github.com/levinliu
 * (Change file header on Settings -> Editor -> File and Code Templates)
 */
public class PrintException {
    private PrintException() {
    }

    public static String print(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
