package com.dslplatform.compiler.client.formatter;

import com.dslplatform.compiler.client.formatter.util.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

enum StaticFormatterFactoryBinder {
    INSTANCE;

    private static InputStream readResource(final String name) throws IOException {
        return StaticFormatterFactoryBinder.class.getResourceAsStream(name);
    }

    private static Properties readProperties(final String name) throws IOException {
        final Properties properties = new Properties();
        properties.load(readResource(name));
        return properties;
    }

    private final Map<String, Formatter> formatters = new HashMap<String, Formatter>();

    public Formatter getFormatter(final String language) {
        final String lowerLang = language.toLowerCase(Locale.ENGLISH);

        synchronized (formatters) {
            final Formatter cachedFormatter = formatters.get(lowerLang);
            if (cachedFormatter != null) return cachedFormatter;

            final Formatter formatter = createFormatter(lowerLang);
            formatters.put(lowerLang, formatter);

            System.out.println("DONE CREATING FORMATERED FOR: " + lowerLang);
            return formatter;
        }
    }

    private static final String DSL_PLATFORM_NEXUS = "https://dsl-platform.com/nexus/content/groups/public/";

    private static final Dependencies csharpDependencies = new Dependencies(
            new ArtifactDownload(DSL_PLATFORM_NEXUS, "com.dslplatform.formatter", "dsl-clc-formatter-language-csharp", "0.2.0", 1522, "9ff1f3505ec9029c69d63bd28b6673314958a3c0")
    );

    private static final Dependencies phpDependencies = new Dependencies(
            new ArtifactDownload(DSL_PLATFORM_NEXUS, "com.dslplatform.formatter", "dsl-clc-formatter-language-php", "0.2.0", 1514, "043768fe7e0e4e1dc4eff1bb05db09538b260e6b")
    );

    private static final Dependencies javaDependencies = new Dependencies(
            new ArtifactDownload(DSL_PLATFORM_NEXUS, "com.dslplatform.formatter", "dsl-clc-formatter-language-java", "0.2.0",                    2122, "98aa1b40e62e78066a751af798408bad99b375be"),
            new ArtifactDownload(DSL_PLATFORM_NEXUS, "org.eclipse.equinox",       "org.eclipse.equinox.common",      "3.6.0.v20100503",        101957, "13c4a5fde7a4b976fe4c5621964881108d23b297"),
            new ArtifactDownload(DSL_PLATFORM_NEXUS, "org.eclipse.jdt",           "core",                            "3.10.0.v20140902-0626", 5565845, "647e19b28c106a63a14401c0f5956289792adf2f"),
            new ArtifactDownload(DSL_PLATFORM_NEXUS, "org.eclipse",               "text",                            "3.5.300.v20130515-1451", 249432, "53576e81d4ea46d7803c1b9fad43a43b5e24b025"),
            new ArtifactDownload(DSL_PLATFORM_NEXUS, "org.eclipse",               "jface",                           "3.10.1.v20140813-1009", 1159354, "cf5197a4a4015c3afe31b265c6aa552dacda6b35")
    );

    private static final Dependencies scala211Dependencies = new Dependencies(
            new ArtifactDownload(DSL_PLATFORM_NEXUS, "org.scala-lang",            "scala-library",                         "2.11.2", 5545300, "4718c022bd5bd84705ddacec18072c351832bc20"),
            new ArtifactDownload(DSL_PLATFORM_NEXUS, "com.dslplatform.formatter", "dsl-clc-formatter-language-scala_2.11", "0.2.0",     2060, "aea98a82812a0bb0e5ee705a031d52c564e9aa14"),
            new ArtifactDownload(DSL_PLATFORM_NEXUS, "com.danieltrinh",           "scalariform_2.11",                      "0.1.5",  1948155, "621e51bc84bd321b4bd003577660fa012e76e92a"),
            new ArtifactDownload(DSL_PLATFORM_NEXUS, "org.scala-lang.modules",    "scala-xml_2.11",                        "1.0.2",   648678, "820fbca7e524b530fdadc594c39d49a21ea0337e")
    );

    @SuppressWarnings("unchecked")
    private Formatter createFormatter(final String language) {
        try {
            if (language.equals("cs")) {
                final Class<Formatter> clazz = (Class<Formatter>) csharpDependencies.getClassLoader()
                        .loadClass("com.dslplatform.compiler.client.formatter.util.CSharpCodeFormatter");

                final Formatter csharpCodeFormatter = clazz.getConstructor().newInstance();

                return new FormatterCombinator(
                        PatternFormatter.fromInputStream(readResource("csharp-clean.regex")),
                        csharpCodeFormatter,
                        new NewlineTrimFormatter("\r\n"));
            }

            if (language.equals("java")) {
                final Class<Formatter> clazz = (Class<Formatter>) javaDependencies.getClassLoader()
                        .loadClass("com.dslplatform.compiler.client.formatter.util.JavaCodeFormatter");

                final Formatter javaCodeFormatter = clazz.getConstructor(Properties.class, String.class)
                        .newInstance(readProperties("java-format.properties"), "\n");

                return new FormatterCombinator(
                        PatternFormatter.fromInputStream(readResource("java-clean.regex")),
                        javaCodeFormatter,
                        new NewlineTrimFormatter("\n"));
            }

            if (language.equals("php")) {
                final Class<Formatter> clazz = (Class<Formatter>) phpDependencies.getClassLoader()
                        .loadClass("com.dslplatform.compiler.client.formatter.util.PHPCodeFormatter");

                final Formatter phpCodeFormatter = clazz.getConstructor().newInstance();

                return new FormatterCombinator(
                        PatternFormatter.fromInputStream(readResource("php-clean.regex")),
                        phpCodeFormatter,
                        new NewlineTrimFormatter("\n"));
            }

            if (language.equals("scala")) {
                final Class<Formatter> clazz = (Class<Formatter>) scala211Dependencies.getClassLoader()
                        .loadClass("com.dslplatform.compiler.client.formatter.util.ScalaCodeFormatter");

                final Formatter scalaCodeFormatter = clazz.getConstructor(Properties.class, String.class, String.class)
                        .newInstance(readProperties("scala-format.properties"), "\n", "2.11.2");

                return new FormatterCombinator(
                        PatternFormatter.fromInputStream(readResource("scala-clean.regex")),
                        scalaCodeFormatter,
                        new NewlineTrimFormatter("\n"));
            }

            return null;
        }
        catch (final Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
