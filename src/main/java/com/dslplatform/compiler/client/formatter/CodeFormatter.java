package com.dslplatform.compiler.client.formatter;

import scala.Option;
import scala.collection.immutable.List;

import scalariform.formatter.ScalaFormatter;
import scalariform.formatter.preferences.IFormattingPreferences;
import scalariform.utils.TextEdit;
import scalariform.utils.TextEditProcessor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

class CodeFormatter {
    public final IFormattingPreferences preferences;

    public static Properties loadDefaultProperties() {
        try {
            final Properties properties = new Properties();
            properties.load(CodeFormatter.class.getResourceAsStream("scalariform.properties"));
            return properties;
        }
        catch (final IOException e) {
            throw new RuntimeException("Could not load scalariform.properties from the classpath!");
        }
    }

    public CodeFormatter() {
        this(loadDefaultProperties());
    }

    public CodeFormatter(final Properties properties) {
        preferences = scalariform.formatter.preferences.PreferencesImporterExporter.getPreferences(properties);
    }

    private String runFormat(final String code) {
        final List<TextEdit> edits = ScalaFormatter.formatAsEdits(code, preferences, Option.apply(null), 0, "2.10.4");
        if (edits.isEmpty()) return code;

        final String formatted = TextEditProcessor.runEdits(code, edits);
        return code.equals(formatted) ? code : formatted;
    }

    private String fixWhitespace(final String code) {
        final String fixed = code
                .replaceAll("\r", "")
                .replaceAll("\t", "  ")
                .replaceAll("\n *?,", ",\n")
                .replaceAll("\\{\\s+?\\}", "{}")
                .replaceAll("\n{2,}\\}", "\n}");

        return code.equals(fixed) ? code : fixed;
    }

    public String format(final String name, final String code) {
        return name.endsWith(".scala")
                ? runFormat(fixWhitespace(code))
                : code;
    }

    public void format(final File file, final String encoding) throws IOException {
        if (!file.isFile()) {
            throw new IOException("File was not a source file");
        }

        final String code;
        {
            final FileInputStream fis = new FileInputStream(file);
            try {
                final byte[] buffer = new byte[(int) file.length()];
                fis.read(buffer);
                code = new String(buffer, encoding);
            } finally {
                fis.close();
            }

        }

        final String formatted = format(file.getName(), code);
        {
            final FileOutputStream fos = new FileOutputStream(file);
            try {
                final byte[] buffer = formatted.getBytes(encoding);
                fos.write(buffer);
            } finally {
                fos.close();
            }
        }
    }

    public static void main(final String[] args) throws IOException {
        final CodeFormatter cf = new CodeFormatter();
        for (final String arg : args) {
            final File file = new File(arg);
            cf.format(file, "UTF-8");
        }
    }
}
