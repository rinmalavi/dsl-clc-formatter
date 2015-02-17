package com.dslplatform.compiler.client.formatter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Format {
	private static final CodeIO codeIO = new CodeIO(Charset.forName("UTF-8"));
	private static final FormatterFactory formatterFactory = FormatterFactory.INSTANCE;

	private static void process(final Formatter formatter, final File file) throws IOException {
		final Code code = codeIO.read(file);
		final String formatted = formatter.format(code.body);
		codeIO.write(code, formatted);
	}

	private static void format(final File file) {
		final Pattern extensionMatch = Pattern.compile("^.*?\\.([^.]+)$");
		final Matcher extensionMatcher = extensionMatch.matcher(file.getName());

		if (extensionMatcher.find()) {
			final String extension = extensionMatcher.group(1).toLowerCase(Locale.ENGLISH);
			final Formatter formatter = FormatterFactory.INSTANCE.getFormatter(extension);

			if (formatter != null) {
				try {
					process(formatter, file);
					return;
				} catch (final Exception e) {}
			}
		}
	}

	public static void formatDirectory(final File directory) {
		final Pattern sourcePattern = Pattern.compile(".*\\.(java|scala|php|cs)");
		if (directory.isDirectory()) {
			for (final File current : new FileIterator(directory, sourcePattern)) {
				format(current);
			}
		} else if (directory.isFile()) {
			format(directory);
		}

	}

}
