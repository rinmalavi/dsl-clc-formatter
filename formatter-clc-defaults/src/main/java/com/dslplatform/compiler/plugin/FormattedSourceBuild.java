package com.dslplatform.compiler.plugin;

import com.dslplatform.compiler.client.Context;
import com.dslplatform.compiler.client.Either;
import com.dslplatform.compiler.client.ExitException;

import com.dslplatform.compiler.client.Utils;
import com.dslplatform.compiler.client.formatter.Format;
import com.dslplatform.compiler.client.parameters.JavaPath;
import com.dslplatform.compiler.client.parameters.Targets;
import com.dslplatform.compiler.client.parameters.build.*;

import java.io.*;
import java.util.List;

/**
 * Overwritten build method.
 * This, custom build, only needs to format the code and, if set, jar it.
 * If target was specified it will also call provided super build.
 */
public enum FormattedSourceBuild implements BuildAction {
	JAVA_CLIENT("java/generated/src", "generated-model-source.jar", Targets.Option.JAVA_CLIENT),
	ANDORID("generated/source", "generated-model-source.jar", Targets.Option.ANDORID)
	/*,
		REVENJ("revenj", "Revenj .NET server", "CSharpServer", ".cs", new CompileRevenj(), false),
		DOTNET_CLIENT("dotnet_client", ".NET client", "CSharpClient", ".cs", new CompileCsClient(".NET client", "client", "dotnet_client", "./ClientModel.dll", DOTNET_CLIENT_DEPENDENCIES, false), false),
		DOTNET_PORTABLE("dotnet_portable", ".NET portable", "CSharpPortable", ".cs", new CompileCsClient(".NET portable", "portable", "dotnet_portable", "./PortableModel.dll", new String[0], false), false),
		DOTNET_WPF("wpf", ".NET WPF GUI", "Wpf", ".cs", new CompileCsClient(".NET WPF GUI", "wpf", "dotnet_wpf", "./WpfModel.dll", DOTNET_WPF_DEPENDENCIES, true), false),
		PHP("php_client", "PHP client", "Php", ".php", new PrepareSources("PHP", "php_client", "Generated-PHP"), true),
		PHP_UI("php_ui", "PHP UI client", "PhpUI", "", new PreparePhpUI("PHP UI", "php_ui", "Generated-PHP-UI"), true),
		SCALA_CLIENT("scala_client", "Scala client", "ScalaClient", ".scala", new CompileScalaClient(), false),
		SCALA_SERVER("scala_server", "Scala server", "ScalaServer", ".scala", new PrepareSources("Scala server", "scala_server", "Generated-Scala-Server"), true)
		*/;

	private final String defaultSourcePath;
	private final String defaultSourcePack;
	final Targets.Option targetOption;
	private BuildAction superBuild = null;

	FormattedSourceBuild(final String defaultSourcePath, final String defaultSourcePack, Targets.Option client) {
		this.defaultSourcePath = defaultSourcePath;
		this.defaultSourcePack = defaultSourcePack;
		this.targetOption = client;
		//	this.formatter = formatter;
	}

	public static FormattedSourceBuild from(final Targets.Option targetOption) {
		for (final FormattedSourceBuild o : FormattedSourceBuild.values()) {
			if (o.targetOption.equals(targetOption)) {
				return o;
			}
		}
		return null;
	}

	public static FormattedSourceBuild from(final Targets.Option targetOption, BuildAction superBuild) {
		for (final FormattedSourceBuild o : FormattedSourceBuild.values()) {
			if (o.targetOption.equals(targetOption)) {
				if (o.superBuild == null)
					o.superBuild = superBuild;
				return o;
			}
		}
		return null;
	}

	@Override
	public boolean check(Context context) throws ExitException {
		return superBuild.check(context);
	}

	@Override
	public void build(File file, Context context) throws ExitException {
		/*  If source was set on this option copy the files to a given location before formatting. */

		File sourceDirectory = copyItMaybe(file, context);

		Format.formatDirectory(file);

		zipItMaybe(sourceDirectory, context);

		/*  Finally, check if the target was called on this option.  */

		buildItMaybe(file, context);

	}

	private File copyItMaybe(File file, Context context) throws ExitException {
		/* check if source option for this target is specified */
		final List<Targets.Option> options = context.load(SourcePlugin.CACHE_NAME);
		if (options != null && options.contains(targetOption)) {
			final String path = context.get(SourcePlugin.sourceOptionCache(targetOption.value));
			final String sourceDirectoryPath = (path == null) ? defaultSourcePath : path;
			try {
				context.log("Copying source to " + sourceDirectoryPath);
				return copyFolder(file, sourceDirectoryPath);
			} catch (IOException e) {
				context.error("IO Error occurred while coping sources: " + e.getMessage());
				throw new ExitException();
			}
		} else
			return file;
	}

	private File copyFolder(File src, String destPath) throws ExitException, IOException {
		final File dest = new File(destPath);
		copyFolder(src, dest);
		return src;
	}
	private void copyFolder(final File src, final File dest) throws ExitException, IOException {
		if (src.isDirectory()) {
			if (!dest.exists()) dest.mkdirs();
			for (String file : src.list()) {
				File srcFile = new File(src, file);
				File destFile = new File(dest, file);
				copyFolder(srcFile, destFile);
			}
		} else {
			InputStream in = new FileInputStream(src);
			OutputStream out = new FileOutputStream(dest);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = in.read(buffer)) > 0) {
				out.write(buffer, 0, length);
			}
			in.close();
			out.close();
		}

	}

	/**
	 * Checks if pack-source for current option was chosen and preforms packing.
	 * @param sourceDirectory
	 * @param context
	 * @throws ExitException
	 */
	private void zipItMaybe(File sourceDirectory, Context context) throws ExitException {
		final List<Targets> packsourceList = context.load(PackSourcePlugin.CACHE_NAME);
		if (packsourceList == null || !packsourceList.contains(this.targetOption)) return;

		final String path = context.load(PackSourcePlugin.sourceOptionCache(targetOption.value));
		final File outputPath = new File(path == null ? defaultSourcePath : path);

		final Either<Utils.CommandResult> execSourceArchive = JavaPath.makeSourcesArchive(context, sourceDirectory, outputPath);
		if (!execSourceArchive.isSuccess()) {
			context.error(execSourceArchive.whyNot());
			throw new ExitException();
		}
	}

	/**
	 * Checks if a given option is in target list and if so calls build of this superbuild.
	 * @param file
	 * @param context
	 * @throws ExitException
	 */
	private void buildItMaybe(File file, Context context) throws ExitException {
		final List<Targets> targetsList = context.load(Targets.CACHE_NAME);
		if (targetsList != null && targetsList.contains(this.targetOption))
			superBuild.build(file, context);
	}
}
