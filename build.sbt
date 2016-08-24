scalaVersion := "2.11.4"
lazy val root = (project in file(".")).
  settings(
    name := "testtoys",
    organization := "com.futurice",
    version := "0.2",
    libraryDependencies ++= Seq(
      "junit" % "junit" % "4.8" % "test",
      "com.novocode" % "junit-interface" % "0.11" % Test
    )
)

