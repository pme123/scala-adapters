// (5) shadow sbt-scalajs' crossProject and CrossType until Scala.js 1.0.0 is released
import sbtcrossproject.{CrossType, crossProject}
import Settings._

lazy val adaptersRoot = project.in(file(".")).
  aggregate(sharedJvm, sharedJs, server, client)
  .settings(organizationSettings)
  .settings(
    publish := {}
    , publishLocal := {}
    , publishArtifact := false
    , isSnapshot := true
    , run := {
      (run in server in Compile).evaluated
    }
  )

lazy val server = (project in file("server"))
  .settings(scalaJSProjects := Seq(client))
  .settings(sharedSettings(Some("server")))
  .settings(serverSettings)
  .settings(serverDependencies)
  .settings(jvmSettings)
  .enablePlugins(PlayScala, BuildInfoPlugin)
  .dependsOn(sharedJvm)



lazy val client = (project in file("client"))
  .settings(sharedSettings(Some("client")))
  .settings(clientSettings)
  .settings(clientDependencies)
  .settings(jsSettings)
  .enablePlugins(ScalaJSWeb)
  .dependsOn(sharedJs)

lazy val shared = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .settings(sharedSettings())
  .settings(sharedDependencies)
  .jsSettings(jsSettings)
  .jsSettings(sharedJsDependencies) // defined in sbt-scalajs-crossproject
  .jvmSettings(jvmSettings)
  .jsConfigure(_ enablePlugins ScalaJSWeb)

lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js
