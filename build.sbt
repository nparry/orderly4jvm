organization := "com.nparry"

name := "orderly"

version := "1.0.2"

description := "An implementation of Orderly JSON (http://orderly-json.org/) for use on the JVM"

licenses += "BSD license" -> url("http://www.opensource.org/licenses/bsd-license.php" )

libraryDependencies ++= Seq(
  "net.liftweb" %% "lift-json" % "2.4",
  "org.scala-tools.testing" %% "specs" % "1.6.9" % "test"
)

publishMavenStyle := true

publishTo <<= (version) { version: String =>
  val repoInfo = if (version.trim.endsWith("SNAPSHOT"))
      ( "nparry snapshots" -> "/home/nparry/repository.nparry.com/snapshots" )
    else
      ( "nparry releases" -> "/home/nparry/repository.nparry.com/releases" )
  val user = System.getProperty("user.name")
  val keyFile = (Path.userHome / ".ssh" / "id_rsa").asFile
  Some(Resolver.ssh(
    repoInfo._1,
    "repository.nparry.com",
    repoInfo._2) as(user, keyFile) withPermissions("0644"))
}

