scalaVersion := "2.12.6"

crossScalaVersions := Seq("2.10.6", "2.11.7")

lazy val testtoys = (project in file(".")).
  settings(
    name := "testtoys",
    organization := "com.futurice",
    version := "0.2",
    libraryDependencies ++= Seq(
      "junit" % "junit" % "4.8" % "test",
      "com.novocode" % "junit-interface" % "0.11" % Test
    )
)

