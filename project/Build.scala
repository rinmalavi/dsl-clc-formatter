import sbt._
import sbt.Keys._

object Build extends Build with Default {
  lazy val interface = (
    project in file("formatter-interface")
    settings(defaultSettings: _*)
    settings(
      name := "DSL-CLC Formatter Interface"
    )
  )

  lazy val util = (
    project in file("formatter-util")
    settings(defaultSettings: _*)
    settings(
      name := "DSL-CLC Formatter Util"
    )
    dependsOn (interface)
  )

  lazy val downloader = (
    project in file("formatter-downloader")
    settings(defaultSettings: _*)
    settings(
      name := "DSL-CLC Formatter Downloader"
    )
  )

// ----------------------------------------------------------------------------

  lazy val languageCSharp = (
    project in file("formatter-language-csharp")
    settings(defaultSettings: _*)
    settings(
      name := "DSL-CLC Formatter Language CSharp"
    )
  ) dependsOn(util)

  lazy val languageJava = (
    project in file("formatter-language-java")
    settings(defaultSettings: _*)
    settings(
      name := "DSL-CLC Formatter Language Java"
    , libraryDependencies ++= Seq(
        "org.eclipse.equinox" % "common" % "3.6.200.v20130402-1505"
      , "org.eclipse.jdt" % "core" % "3.10.0.v20140902-0626"
      , "org.eclipse" % "jface" % "3.10.1.v20140813-1009"
      , "org.eclipse" % "text" % "3.5.300.v20130515-1451"
      )
    )
  ) dependsOn(util)

  lazy val languagePHP = (
    project in file("formatter-language-php")
    settings(defaultSettings: _*)
    settings(
      name := "DSL-CLC Formatter Language PHP"
    )
  ) dependsOn (util)

  lazy val languageScala = (
    project in file("formatter-language-scala")
    settings(defaultSettings: _*)
    settings(
      name := "DSL-CLC Formatter Language Scala"
    , crossPaths := true
    , libraryDependencies += "com.danieltrinh" %% "scalariform" % "0.1.5"
    )
  ) dependsOn(util)

// ----------------------------------------------------------------------------

  import sbtassembly.AssemblyPlugin.autoImport._

  lazy val clcDefaults = (
    project in file("formatter-clc-defaults")
    settings(defaultSettings: _*)
    settings(
      name := "DSL-CLC Formatter Defaults"
    , assemblyJarName in assembly := "dsl-clc-formatter.jar"
    , mainClass in assembly := Some("com.dslplatform.compiler.client.formatter.Main")
    , libraryDependencies += "junit" % "junit" % "4.11" % "test"
    , libraryDependencies += "commons-io" % "commons-io" % "2.4" % "test"
    ) settings (ScriptedPlugin.scriptedSettings: _*)
      settings(ScriptedPlugin.scriptedLaunchOpts := {
          ScriptedPlugin.scriptedLaunchOpts.value ++
            Seq("-Xmx1024M"
              , "-XX:MaxPermSize=256M"
              , "-Dlibrary.version=" + version.value
            )
        }
      )
    ) dependsOn(downloader, util)

  lazy val root = (
    project in file(".")
    settings(defaultSettings: _*)
    aggregate(
      interface
    , util
    , languageCSharp
    , languageJava
    , languagePHP
    , languageScala
    , downloader
    , clcDefaults
    )
  )
}
