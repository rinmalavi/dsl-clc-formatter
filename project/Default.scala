import sbt.Keys._
import sbt._

trait Default {
  private val NGSNexus     = "NGS Nexus"     at "http://ngs.hr/nexus/content/groups/public/"
  private val NGSReleases  = "NGS Releases"  at "http://ngs.hr/nexus/content/repositories/releases/"
  private val NGSSnapshots = "NGS Snapshots" at "http://ngs.hr/nexus/content/repositories/snapshots/"

  val defaultSettings =
    Defaults.coreDefaultSettings ++
    net.virtualvoid.sbt.graph.Plugin.graphSettings ++
    sbtrelease.ReleasePlugin.releaseSettings ++ 
    sbtassembly.Plugin.assemblySettings ++ Seq(
      organization := "com.dslplatform.formatter"
    , autoScalaLibrary := false
    , crossPaths := false
    , javaHome := sys.env.get("JDK16_HOME").map(file(_))
    , javacOptions := Seq(
        "-deprecation"
      , "-encoding", "UTF-8"
      , "-Xlint:unchecked"
      , "-source", "1.6"
      , "-target", "1.6"
      )
    , crossScalaVersions := Seq("2.11.2", "2.10.4")
    , scalaVersion := crossScalaVersions.value.head
    , scalacOptions := Seq(
        "-deprecation"
      , "-encoding", "UTF-8"
      , "-feature"
      , "-language:existentials"
      , "-language:implicitConversions"
      , "-language:postfixOps"
      , "-language:reflectiveCalls"
      , "-optimise"
      , "-unchecked"
      , "-Xcheckinit"
      , "-Xlint"
      , "-Xmax-classfile-name", "72"
      , "-Xno-forwarders"
      , "-Xverify"
      , "-Yclosure-elim"
      , "-Ydead-code"
      , "-Yinline-warnings"
      , "-Yinline"
      , "-Yrepl-sync"
      , "-Ywarn-adapted-args"
      , "-Ywarn-dead-code"
      , "-Ywarn-inaccessible"
      , "-Ywarn-nullary-override"
      , "-Ywarn-nullary-unit"
      , "-Ywarn-numeric-widen"
      )
    , resolvers := Seq(NGSNexus)
    , externalResolvers := Resolver.withDefaultResolvers(resolvers.value, mavenCentral = false)
    , publishTo := Some(if (version.value endsWith "-SNAPSHOT") NGSSnapshots else NGSReleases)
    , publishArtifact in (Compile, packageDoc) := false
    , credentials ++= {
        val creds = Path.userHome / ".config" / "dsl-clc-formatter" / "nexus.config"
        if (creds.exists) Some(Credentials(creds)) else None
      }.toSeq
    )
}
