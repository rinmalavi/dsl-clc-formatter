import AssemblyKeys._

organization := "com.dslplatform"

name := "dsl-clc-formatter"

version := "0.0.1"

scalaVersion := "2.11.2"

crossPaths := false

libraryDependencies := Seq(
  "org.scala-lang" % "scala-library" % scalaVersion.value
, "com.danieltrinh" %% "scalariform" % "0.1.5" intransitive()
)

graphSettings

assemblySettings

jarName in assembly := "dsl-clc-formatter.jar"

mainClass in assembly := Some("com.dslplatform.compiler.client.formatter.CodeFormatter")
