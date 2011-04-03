import sbt._

class ScalaRestProject(info: ProjectInfo) extends DefaultProject(info) with IdeaProject {

    /**
     * Scala repository
     */
    val scalaRepo = "Scala Repo" at "http://scala-tools.org/repo-releases"

    /**
     * Import Scala Specs
     */
    val scalaSpecs = "org.scala-tools.testing" % "specs_2.8.1" % "1.6.7"

    /**
     * Import Scala Tests
     */
    val scalaTest = "org.scalatest" % "scalatest" % "1.3"

    /**
     * Jetty Embedded;
     */
    val jetty = "org.mortbay.jetty" % "jetty-embedded" % "6.1.26"

}