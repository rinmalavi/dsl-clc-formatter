addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.6.0")
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.4")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.12.0")

libraryDependencies <+= sbtVersion("org.scala-sbt" % "scripted-plugin" % _)
