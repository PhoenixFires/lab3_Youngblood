import sbt._
import Keys._

object lab3Build extends Build {
  lazy val root = Project(id = "lab3",
                          base = file("."))

  lazy val grader = Project(id = "lab3-grader",
                            base = file("grader")) dependsOn(root)
}
