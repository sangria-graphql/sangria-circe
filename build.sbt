val isScala3 = Def.setting(
  CrossVersion.partialVersion(scalaVersion.value).exists(_._1 == 3)
)

name := "sangria-circe"
organization := "org.sangria-graphql"
mimaPreviousArtifacts := {
  if (isScala3.value)
    Set.empty
  else
    Set("org.sangria-graphql" %% "sangria-circe" % "1.3.1")
}

description := "Sangria circe marshalling"
homepage := Some(url("http://sangria-graphql.org"))
licenses := Seq(
  "Apache License, ASL Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))

ThisBuild / crossScalaVersions := Seq("2.12.16", "2.13.8", "3.2.0")
ThisBuild / scalaVersion := crossScalaVersions.value.last
ThisBuild / githubWorkflowPublishTargetBranches := List()
ThisBuild / githubWorkflowBuildPreamble ++= List(
  WorkflowStep.Sbt(List("mimaReportBinaryIssues"), name = Some("Check binary compatibility")),
  WorkflowStep.Sbt(List("scalafmtCheckAll"), name = Some("Check formatting"))
)

scalacOptions ++= Seq("-deprecation", "-feature")
scalacOptions ++= {
  if (isScala3.value)
    Seq("-Xtarget:8")
  else
    Seq("-target:jvm-1.8")
}
javacOptions ++= Seq("-source", "8", "-target", "8")

val circeVersion = "0.14.2"

libraryDependencies ++= Seq(
  "org.sangria-graphql" %% "sangria-marshalling-api" % "1.0.8",
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion % Test,
  "org.sangria-graphql" %% "sangria-marshalling-testkit" % "1.0.4" % Test,
  "org.scalatest" %% "scalatest" % "3.2.13" % Test
)

// Publishing
ThisBuild / githubWorkflowTargetTags ++= Seq("v*")
ThisBuild / githubWorkflowPublishTargetBranches :=
  Seq(RefPredicate.StartsWith(Ref.Tag("v")))
ThisBuild / githubWorkflowPublish := Seq(
  WorkflowStep.Sbt(
    List("ci-release"),
    env = Map(
      "PGP_PASSPHRASE" -> "${{ secrets.PGP_PASSPHRASE }}",
      "PGP_SECRET" -> "${{ secrets.PGP_SECRET }}",
      "SONATYPE_PASSWORD" -> "${{ secrets.SONATYPE_PASSWORD }}",
      "SONATYPE_USERNAME" -> "${{ secrets.SONATYPE_USERNAME }}"
    )
  )
)

startYear := Some(2016)
organizationHomepage := Some(url("https://github.com/sangria-graphql"))
developers := Developer(
  "OlegIlyenko",
  "Oleg Ilyenko",
  "",
  url("https://github.com/OlegIlyenko")) :: Nil
scmInfo := Some(
  ScmInfo(
    browseUrl = url("https://github.com/sangria-graphql/sangria-circe"),
    connection = "scm:git:git@github.com:sangria-graphql/sangria-circe.git"
  ))

// nice *magenta* prompt!
ThisBuild / shellPrompt := { state =>
  scala.Console.MAGENTA + Project.extract(state).currentRef.project + "> " + scala.Console.RESET
}
