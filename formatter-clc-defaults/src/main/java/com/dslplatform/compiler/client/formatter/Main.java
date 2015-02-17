package com.dslplatform.compiler.client.formatter;

import java.io.*;
public class Main {

    public static void main(final String[] args) {
        for (final String arg : args) {
            final File file = new File(arg);
			Format.formatDirectory(file);
        }
    }
}
