import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "exchange"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    javaCore,
    "com.netflix.astyanax" % "astyanax" % "1.56.26"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here      
  )

}
