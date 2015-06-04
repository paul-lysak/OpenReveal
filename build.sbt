name := "OpenReveal"

version := "0.0"

scalaVersion := "2.11.6"



libraryDependencies ++= List(
  "joda-time" % "joda-time" % "2.3",
  "org.apache.jena" %   "jena-core" % "2.13.0",
  "org.apache.jena" %   "jena-tdb" % "1.1.2",
  "commons-io"      %   "commons-io" % "2.4" % "test",
  "org.scalatest"   %%  "scalatest" % "2.2.4" % "test"
)


