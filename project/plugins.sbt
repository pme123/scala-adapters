// Comment to get more information during initialization
logLevel := Level.Warn

// Resolvers
resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"

resolvers += Resolver.url("heroku-sbt-plugin-releases",
  url("https://dl.bintray.com/heroku/sbt-plugins/"))(Resolver.ivyStylePatterns)

resolvers += "jitpack" at "https://jitpack.io"

// Sbt plugins
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.6.6")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.6")

addSbtPlugin("com.vmunier" % "sbt-web-scalajs" % "1.0.6")

addSbtPlugin("com.typesafe.sbt" % "sbt-gzip" % "1.0.2")

addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.1.3")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.1")
addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.2.2")

// see https://github.com/portable-scala/sbt-crossproject
addSbtPlugin("org.scala-js"     % "sbt-scalajs"              % "0.6.21")
addSbtPlugin("org.portable-scala" % "sbt-crossproject"         % "0.3.1")  // (1)
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "0.3.1")  // (2)

// version infos
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.7.0")
