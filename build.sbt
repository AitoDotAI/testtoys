
lazy val root = (project in file(".")).
  settings(
    scalaVersion := "2.11.7",
    name := "testtoys",
    organization := "com.futurice",
    version := "0.1-SNAPSHOT",
    libraryDependencies ++= Seq(
      "junit" % "junit" % "4.8" % "test",
      "com.novocode" % "junit-interface" % "0.11" % Test
    )
)
