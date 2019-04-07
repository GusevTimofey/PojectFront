
name := "ProjectFront"

version := "1.0"

lazy val `ProjectFront` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

scalaVersion := "2.12.2"

PlayKeys.devSettings := Seq("play.server.http.port" -> "9053")

libraryDependencies ++= Seq(
  "org.scodec" %% "scodec-core" % "1.10.3",
  "io.circe" %% "circe-generic" % "0.9.3",
  "io.circe" %% "circe-core" % "0.9.3",
  "io.circe" %% "circe-parser" % "0.9.3",
  "com.typesafe.play" %% "play" % "2.7.0",
  "com.google.inject" % "guice" % "4.2.2",
  "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",
  jdbc, ehcache, ws, specs2 % Test, guice
)

unmanagedResourceDirectories in Test <+= baseDirectory(_ / "target/web/public/test")

PB.targets in Compile := Seq(
  scalapb.gen() -> (sourceManaged in Compile).value / "protobuf"
)