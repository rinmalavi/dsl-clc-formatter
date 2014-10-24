package com.dslplatform.compiler.client.formatter.util;

import com.dslplatform.compiler.client.formatter.Formatter;

public abstract class NoopFormatter implements Formatter {
    @Override
    public String format(final String body) {
        return body;
    }
}
