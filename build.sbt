organization := "com.nparry"

name := "orderly"

version := "1.1.0-SNAPSHOT"

description := "An implementation of Orderly JSON (http://orderly-json.org/) for use on the JVM"

homepage := Some(url("https://github.com/nparry/orderly4jvm"))

licenses += "BSD" -> url("http://www.opensource.org/licenses/bsd-license.php" )

scmInfo := Some(ScmInfo(url("https://github.com/nparry/orderly4jvm.git"),
  "git@github.com:nparry/orderly4jvm.git"))

pomExtra := (
  <developers>
    <developer>
      <id>nparry</id>
      <name>Nathan Parry</name>
      <url>http://nparry.com</url>
    </developer>
  </developers>
)

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.6",
  "net.liftweb" %% "lift-json" % "3.1.0",
  "org.specs2" %% "specs2-core" % "3.9.5" % "test"
)

crossScalaVersions := Seq("2.12.2", "2.11.11")
scalaVersion := crossScalaVersions.value.head

publishMavenStyle := true

pomIncludeRepository := { _ => false }

pgpSecretRing := file(sys.props("user.home") + "/.bintray/bintray.asc")

pgpPassphrase := Some(scala.util.Try(sys.env("BINTRAY_PASSPHRASE")).getOrElse("fail").toCharArray)

seq(bintraySettings:_*)

bintray.Keys.packageLabels in bintray.Keys.bintray := Seq("json", "orderly")

