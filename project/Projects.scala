import sbt._
import sbt.Keys._

object Projects extends Build with Default {
  lazy val interface = Project(
    "interface"
  , file("formatter-interface")
  , settings = defaultSettings ++ Seq(
      name := "DSL-CLC Formatter Interface"
    )
  )

  lazy val util = Project(
    "util"
  , file("formatter-util")
  , settings = defaultSettings ++ Seq(
      name := "DSL-CLC Formatter Util"
    )
  ) dependsOn (interface)

  lazy val downloader = Project(
    "downloader"
  , file("formatter-downloader")
  , settings = defaultSettings ++ Seq(
      name := "DSL-CLC Formatter Downloader"
    )
  )

// ----------------------------------------------------------------------------

  lazy val languageCSharp = Project(
    "language-csharp"
  , file("formatter-language-csharp")
  , settings = defaultSettings ++ Seq(
      name := "DSL-CLC Formatter Language CSharp"
    )
  ) dependsOn (util)

  lazy val languageJava = Project(
    "language-java"
  , file("formatter-language-java")
  , settings = defaultSettings ++ Seq(
      name := "DSL-CLC Formatter Language Java"
    , libraryDependencies ++= Seq(
        "org.eclipse.equinox" % "common" % "3.6.200.v20130402-1505"
      , "org.eclipse.jdt" % "core" % "3.10.0.v20140902-0626"
      , "org.eclipse" % "jface" % "3.10.1.v20140813-1009"
      , "org.eclipse" % "text" % "3.5.300.v20130515-1451"
      )
    )
  ) dependsOn (util)

  lazy val languagePHP = Project(
    "language-php"
  , file("formatter-language-php")
  , settings = defaultSettings ++ Seq(
      name := "DSL-CLC Formatter Language PHP"
    )
  ) dependsOn (util)

  lazy val languageScala = Project(
    "language-scala"
  , file("formatter-language-scala")
  , settings = defaultSettings ++ Seq(
      name := "DSL-CLC Formatter Language Scala"
    , autoScalaLibrary := true
    , crossPaths := true
    , libraryDependencies ++= Seq(
        "com.danieltrinh" %% "scalariform" % "0.1.5"
      )
    )
  ) dependsOn (util)

// ----------------------------------------------------------------------------

  import sbtassembly.Plugin.AssemblyKeys._

  lazy val clcDefaults = Project(
    "clc-defaults"
  , file("formatter-clc-defaults")
  , settings = defaultSettings ++ Seq(
      name := "DSL-CLC Formatter Defaults"
    , jarName in assembly := "dsl-clc-formatter.jar"
    , mainClass in assembly := Some("com.dslplatform.compiler.client.formatter.Main")
    )
  ) dependsOn (downloader, util)

  val root = (project in file(".")) settings (defaultSettings: _*) aggregate(
    interface
  , util
  , languageCSharp
  , languageJava
  , languagePHP
  , languageScala
  , downloader
  , clcDefaults
  )
}
