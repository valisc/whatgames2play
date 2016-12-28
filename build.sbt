import sbt.Keys._

name := """whatgames2play"""
organization := "net.inkanito"


lazy val commonSettings = Seq(
  version := "1.0",
  scalaVersion := "2.11.8"
)

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

lazy val client = (project in file("client"))
  .settings(commonSettings: _*)
  .settings(
  persistLauncher := true,
  persistLauncher in Test := false,
  libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom" % "0.9.1",
    "co.technius" %%% "scalajs-mithril" % "0.1.0"
  )
).enablePlugins(ScalaJSPlugin, ScalaJSWeb, SbtWeb).dependsOn(sharedJs)

lazy val web_server = (project in file("web_server"))
  .settings(commonSettings: _*)
  .settings(
    scalaJSProjects := Seq(client),
    pipelineStages in Assets := Seq(scalaJSPipeline),
    compile in Compile <<= (compile in Compile) dependsOn scalaJSPipeline,
    WebKeys.packagePrefix in Assets := "public/",
    managedClasspath in Runtime += (packageBin in Assets).value,
    Revolver.settings,
    fork in run := true,
    libraryDependencies ++= {
      val akkaV       = "2.4.3"
      val scalaTestV  = "2.2.6"
      val slickV      = "3.0.1"
      Seq(
        "com.typesafe.akka" %% "akka-actor" % akkaV,
        "com.typesafe.akka" %% "akka-stream" % akkaV,
        "com.typesafe.akka" %% "akka-http-experimental" % akkaV,
        "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaV,
        "com.typesafe.akka" %% "akka-http-testkit" % akkaV,
        "org.webjars" % "webjars-locator" % "0.32",
        "org.scalatest"     %% "scalatest" % scalaTestV % "test"
      )
    }
  )
  .enablePlugins(JavaAppPackaging, SbtWeb).dependsOn(sharedJvm)

lazy val shared = (crossProject.crossType(CrossType.Pure) in file("shared"))
    .settings(commonSettings: _*)
    .jsConfigure(_ enablePlugins ScalaJSWeb)

lazy val root = (project in file("."))
  .settings(commonSettings: _*)
  .aggregate(web_server, client)

lazy val sharedJvm = shared.jvm

lazy val sharedJs = shared.js



onLoad in Global := (Command.process("project web_server", _: State)) compose (onLoad in Global).value
