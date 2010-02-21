import sbt._

class OrderlyProject(info: ProjectInfo) extends DefaultProject(info) {
  val lift_json = "net.liftweb" % "lift-json" % "2.0-M2"
}

