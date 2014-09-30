package com.dslplatform.compiler.client.formatter.impl;

import com.dslplatform.compiler.client.formatter.Formatter;

import scala.Some;
import scala.collection.immutable.List;

import scalariform.formatter.ScalaFormatter;
import scalariform.formatter.preferences.IFormattingPreferences;
import scalariform.formatter.preferences.PreferencesImporterExporter;
import scalariform.utils.TextEdit;
import scalariform.utils.TextEditProcessor;

import java.util.Properties;

public class ScalaCodeFormatter implements Formatter {
    private final IFormattingPreferences preferences;
    private final Some<String> newline;
    private final String scalaVersion;

    public ScalaCodeFormatter(final Properties properties, final String newline, final String scalaVersion) {
        this.preferences = PreferencesImporterExporter.getPreferences(properties);
        this.newline = new Some<String>(newline);
        this.scalaVersion = scalaVersion;
    }

    @Override
    public String format(final String code) {
        final List<TextEdit> edits = ScalaFormatter.formatAsEdits(code, preferences, newline, 0, scalaVersion);
        if (edits.isEmpty()) return code;

        final String formatted = TextEditProcessor.runEdits(code, edits);
        return code.equals(formatted) ? code : formatted;
    }
}
