import com.typesafe.sbt.digest.Import.{DigestKeys, digest}
import com.typesafe.sbt.gzip.Import.gzip
import com.typesafe.sbt.web.Import.Assets
import com.typesafe.sbt.web.SbtWeb.autoImport.pipelineStages
import org.scalajs.sbtplugin.ScalaJSPlugin.AutoImport.jsDependencies
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import org.scalajs.sbtplugin.Stage
import play.sbt.PlayImport.{filters, guice, ws}
import sbt.Keys._
import sbt.{Def, ExclusionRule, URL, _}
import sbtbuildinfo.BuildInfoPlugin.autoImport._
import webscalajs.WebScalaJS.autoImport.scalaJSPipeline

object Settings {

  lazy val orgId = "pme123"
  lazy val orgHomepage = Some(new URL("https://github.com/pme123"))
  lazy val projectName = "scala-adapters"
  lazy val projectV = "1.1.0"

  // main versions
  lazy val scalaV = "2.12.4"
  lazy val bindingV = "11.0.1"
  lazy val jQueryV = "2.2.4"
  lazy val sloggingV = "0.6.0"
  lazy val semanticV = "2.2.10"
  lazy val scalaTestV = "3.0.4"

  lazy val organizationSettings = Seq(
    organization := orgId
    , organizationHomepage := orgHomepage
  )

  lazy val testStage: Stage = sys.props.get("testOpt").map {
    case "full" => FullOptStage
    case "fast" => FastOptStage
  }.getOrElse(FastOptStage)

  lazy val serverSettings: Seq[Def.Setting[_]] = Def.settings(
    buildInfoSettings
    , pipelineStages in Assets := Seq(scalaJSPipeline)
    , pipelineStages := Seq(digest, gzip)
    // triggers scalaJSPipeline when using compile or continuous compilation
    , compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value
    // to have routing also in ScalaJS
    // Create a map of versioned assets, replacing the empty versioned.js
    , DigestKeys.indexPath := Some("javascripts/versioned.js")
    // Assign the asset index to a global versioned var
    , DigestKeys.indexWriter ~= { writer => index => s"var versioned = ${writer(index)};" }
  )

  lazy val serverDependencies: Seq[Def.Setting[_]] = Def.settings(libraryDependencies ++= Seq(
    ws
    , guice
    , filters
    , "org.apache.commons" % "commons-email" % "1.3.1"
    , "biz.enef" %% "slogging-slf4j" % sloggingV
    // scalajs for server
    , "com.vmunier" %% "scalajs-scripts" % "1.1.1"
    // webjars for Semantic-UI
    , "org.webjars" %% "webjars-play" % "2.6.1"
    , "org.webjars" % "Semantic-UI" % semanticV
    , "org.webjars" % "jquery" % jQueryV
    // metrics
    , "com.kenshoo" %% "metrics-play" % "2.6.6_0.6.2"
    // TEST
    , "com.typesafe.akka" %% "akka-testkit" % "2.5.6" % Test
    , "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.6" % Test
    , "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
    , "org.awaitility" % "awaitility" % "3.0.0" % Test

    , "org.scalatest" %% "scalatest" % scalaTestV % Test
    , "org.scalamock" %% "scalamock-scalatest-support" % "3.4.2" % Test
    , "org.subethamail" % "subethasmtp" % "3.1.7" % Test
  ).map(_.excludeAll(ExclusionRule("org.slf4j", "slf4j-log4j12")))
  )

  lazy val clientSettings = Seq(
    scalacOptions ++= Seq("-Xmax-classfile-name", "78")
    , addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
    , jsDependencies ++= Seq(
      "org.webjars" % "jquery" % jQueryV / "jquery.js" minified "jquery.min.js"
      , "org.webjars" % "Semantic-UI" % semanticV / "semantic.js" minified "semantic.min.js" dependsOn "jquery.js"
    )
  )
  lazy val clientDependencies: Seq[Def.Setting[_]] = Def.settings(libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom" % "0.9.3"
    , "org.scala-lang.modules" %% "scala-xml" % "1.0.6"
    , "com.typesafe.play" %%% "play-json" % "2.6.1"
    , "com.thoughtworks.binding" %%% "dom" % bindingV
    , "com.thoughtworks.binding" %%% "futurebinding" % bindingV
    , "fr.hmil" %%% "roshttp" % "2.0.2"
    // java.time support for ScalaJS
    , "org.scala-js" %%% "scalajs-java-time" % "0.2.2"
    // jquery support for ScalaJS
    , "be.doeraene" %%% "scalajs-jquery" % "0.9.1"
    , "org.scalatest" %%% "scalatest" % scalaTestV % Test
  ))

  lazy val sharedDependencies: Seq[Def.Setting[_]] = Def.settings(libraryDependencies ++= Seq(
    "org.julienrf" %%% "play-json-derived-codecs" % "4.0.0"
    // logging lib that also works with ScalaJS
    , "biz.enef" %%% "slogging" % sloggingV
    , "org.scalatest" %%% "scalatest" % scalaTestV % Test

  ))

  lazy val jsSettings: Seq[Def.Setting[_]] = Seq(
    scalaJSStage in Global := testStage
  )

  lazy val sharedJsDependencies: Seq[Def.Setting[_]] = Def.settings(libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-java-time" % "0.2.3"
  ))

  def sharedSettings(moduleName: Option[String] = None): Seq[Def.Setting[_]] = Seq(
    scalaVersion := scalaV
    , name := s"$projectName${moduleName.map("-" + _).getOrElse("")}"
    , version := s"$projectV"
    , publishArtifact in(Compile, packageDoc) := false
    , publishArtifact in packageDoc := false
    , sources in(Compile, doc) := Seq.empty
  ) ++ organizationSettings

  private lazy val buildInfoSettings = Seq(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoOptions += BuildInfoOption.BuildTime,
    buildInfoOptions += BuildInfoOption.ToJson,
    buildInfoPackage := "pme123.adapters.version"
  )

}
