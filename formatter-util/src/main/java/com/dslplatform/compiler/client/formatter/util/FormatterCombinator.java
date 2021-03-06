package com.dslplatform.compiler.client.formatter.util;

import com.dslplatform.compiler.client.formatter.Formatter;

public class FormatterCombinator implements Formatter {
    private final Formatter[] formatters;

    public FormatterCombinator(final Formatter... formatters) {
        this.formatters = formatters;
    }

    @Override
    public String format(final String body) {
        String work = body;
        for (final Formatter formatter : formatters) {
            work = formatter.format(work);
        }
        return work;
    }
}
