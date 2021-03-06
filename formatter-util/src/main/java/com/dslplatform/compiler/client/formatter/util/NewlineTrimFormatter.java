package com.dslplatform.compiler.client.formatter.util;

import com.dslplatform.compiler.client.formatter.Formatter;

public class NewlineTrimFormatter implements Formatter {
    private final String newline;

    public NewlineTrimFormatter(final String newline) {
        this.newline = newline;
    }

    @Override
    public String format(final String body) {
        final int nlen = newline.length();
        final int blen = body.length();
        if (nlen == 0 || blen == 0) return body;

        int start = 0;
        while (body.startsWith(newline, start)) {
            start += nlen;
        }

        if (start == blen) {
            return start == nlen ? body : newline;
        }

        int end = blen;
        while (end >= nlen && body.startsWith(newline, end - nlen)) {
            end -= nlen;
        }

        return end == blen - nlen
                ? body.substring(start)
                : end == blen
                        ? body.substring(start) + newline
                        : body.substring(start, end + nlen);
    }
}
