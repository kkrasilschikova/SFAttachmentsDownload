import sbtassembly.MergeStrategy

name := "download"

version := "0.1"

scalaVersion := "2.12.3"

mainClass in assembly := Some("SfAttachDownload.Main")

assemblyJarName in assembly := "download.jar"

assemblyOutputPath in assembly := file("./download.jar")

libraryDependencies += "com.force.api" % "force-partner-api" % "40.0.0"
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.6.0"
libraryDependencies += "com.github.scopt" %% "scopt" % "3.7.0"
libraryDependencies += "org.typelevel" %% "cats-core" % "1.0.0-RC1"
libraryDependencies += "org.typelevel" %% "cats-effect" % "0.5"
libraryDependencies += "commons-io" % "commons-io" % "2.4"
libraryDependencies += "commons-net" % "commons-net" % "3.6"

scalacOptions ++= Seq("-language:higherKinds", "-Ywarn-unused", "-Ywarn-unused-import", "-Ypartial-unification")

assemblyMergeStrategy in assembly := {
  case PathList("javax", "servlet", xs @ _*)         => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".html" => MergeStrategy.first
  case "application.conf"                            => MergeStrategy.concat
  case "unwanted.txt"                                => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

val defaultMergeStrategy: String => MergeStrategy = {
  case x if Assembly.isConfigFile(x) =>
    MergeStrategy.concat
  case PathList(ps @ _*) if Assembly.isReadme(ps.last) || Assembly.isLicenseFile(ps.last) =>
    MergeStrategy.rename
  case PathList("META-INF", xs @ _*) =>
    (xs map {_.toLowerCase}) match {
      case ("manifest.mf" :: Nil) | ("index.list" :: Nil) | ("dependencies" :: Nil) =>
        MergeStrategy.discard
      case ps @ (x :: xs) if ps.last.endsWith(".sf") || ps.last.endsWith(".dsa") =>
        MergeStrategy.discard
      case "plexus" :: xs =>
        MergeStrategy.discard
      case "services" :: xs =>
        MergeStrategy.filterDistinctLines
      case ("spring.schemas" :: Nil) | ("spring.handlers" :: Nil) =>
        MergeStrategy.filterDistinctLines
      case _ => MergeStrategy.deduplicate
    }
  case _ => MergeStrategy.deduplicate
}