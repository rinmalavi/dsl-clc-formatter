package com.dslplatform.compiler.client.formatter;

import com.dslplatform.compiler.client.formatter.impl.*;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    private final CodeIO codeIO;
    private final Map<String, Formatter> formatters;

    public Main() {
        this.codeIO = new CodeIO(Charset.forName("UTF-8"));
        this.formatters = new HashMap<String, Formatter>();

        try {
            final Properties javaFormatProperties = new Properties();
            final InputStream javaCleanRegex = Main.class.getResourceAsStream("java-clean.regex");
            javaFormatProperties.load(Main.class.getResourceAsStream("java-format.properties"));
            this.formatters.put("java", new FormatterCombinator(
                    PatternFormatter.fromInputStream(javaCleanRegex),
                    new JavaCodeFormatter(javaFormatProperties, "\n"),
                    new NewlineTrimFormatter("\n")));

            final Properties phpFormatProperties = new Properties();
            final InputStream phpCleanRegex = Main.class.getResourceAsStream("php-clean.regex");
            this.formatters.put("php", new FormatterCombinator(
                    PatternFormatter.fromInputStream(phpCleanRegex),
                    new NewlineTrimFormatter("\n")));

            final Properties csharpFormatProperties = new Properties();
            final InputStream csharpCleanRegex = Main.class.getResourceAsStream("csharp-clean.regex");
            this.formatters.put("cs", new FormatterCombinator(
                    PatternFormatter.fromInputStream(csharpCleanRegex),
                    new NewlineTrimFormatter("\r\n")));

            final InputStream scalaCleanRegex = Main.class.getResourceAsStream("scala-clean.regex");
            final Properties scalaFormatProperties = new Properties();
            scalaFormatProperties.load(Main.class.getResourceAsStream("scala-format.properties"));
            this.formatters.put("scala", new FormatterCombinator(
                    PatternFormatter.fromInputStream(scalaCleanRegex),
                    new ScalaCodeFormatter(scalaFormatProperties, "\n", "2.10.4"),
                    new NewlineTrimFormatter("\n")));
        }
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void process(final Formatter formatter, final File file) throws IOException {
        final Code code = codeIO.read(file);
        final String formatted = formatter.format(code.body);
        codeIO.write(code, formatted);
    }

    private void format(final File file) {
        final Pattern extensionMatch = Pattern.compile("^.*?\\.([^.]+)$");
        final Matcher extensionMatcher = extensionMatch.matcher(file.getName());

        if (extensionMatcher.find()) {
            final String extension = extensionMatcher.group(1).toLowerCase(Locale.ENGLISH);
            final Formatter formatter = formatters.get(extension);

            if (formatter != null) {
                try {
                    System.out.println("Processing: " + file);
                    process(formatter, file);
                    return;
                } catch (final Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        }

        System.err.println("Don't know how to format: " + file);
    }

    public static void main(final String[] args) throws Exception {
        final Pattern sourcePattern = Pattern.compile(".*(java|scala|php|cs)");
        final Main main = new Main();

        for (final String arg : args) {
            final File file = new File(arg);
            if (file.isDirectory()) {
                for (final File current : new FileIterator(file, sourcePattern)) {
                    main.format(current);
                }
            } else if (file.isFile()) {
                main.format(file);
            } else {
                System.err.println("Could not format: " + file);
            }
        }
    }
}
