import sbt._

class OrderlyProject(info: ProjectInfo) extends DefaultProject(info) {
  val lift_json = "net.liftweb" %% "lift-json" % "2.3"
  val specs = "org.scala-tools.testing" % "specs_2.8.0" % "1.6.5" % "test"

  override def compileOrder = CompileOrder.JavaThenScala
}

