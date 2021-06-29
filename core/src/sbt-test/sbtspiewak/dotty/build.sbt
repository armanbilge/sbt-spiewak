ThisBuild / crossScalaVersions := Seq("2.13.6", "3.0.0-RC1", "3.0.0-RC2", "3.0.0-RC3", "3.0.0")

ThisBuild / baseVersion := "0.1"

ThisBuild / publishGithubUser := "djspiewak"
ThisBuild / publishFullName := "Daniel Spiewak"

lazy val check = taskKey[Unit]("")

check := {
  val suffix = if (isDotty.value) "3" else "2"
  List((Compile / unmanagedSourceDirectories).value, (Test / unmanagedSourceDirectories).value) foreach { dirs =>
    if (!dirs.exists(_.getAbsolutePath().endsWith(s"scala-$suffix"))) {
      sys.error(s"$dirs did not contain a scala-$suffix directory")
    }
  }
}
