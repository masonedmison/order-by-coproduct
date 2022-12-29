val scala213 = "2.13.8"
val scala212 = "2.12.14"

ThisBuild / scalaVersion := scala213
ThisBuild / crossScalaVersions := Seq(scala212, scala213)

//sbt-ci-release settings
ThisBuild / githubWorkflowTargetTags ++= Seq("v*")
ThisBuild / githubWorkflowPublishTargetBranches := Seq(
  RefPredicate.StartsWith(Ref.Branch("main")),
  RefPredicate.StartsWith(Ref.Tag("v"))
)
ThisBuild / githubWorkflowPublishPreamble := Seq(
  WorkflowStep.Use(UseRef.Public("olafurpg", "setup-gpg", "v3"))
)
ThisBuild / githubWorkflowPublish := Seq(WorkflowStep.Sbt(List("ci-release")))
ThisBuild / githubWorkflowEnv ++= List(
  "PGP_PASSPHRASE",
  "PGP_SECRET",
  "SONATYPE_PASSWORD",
  "SONATYPE_USERNAME"
).map(envKey => envKey -> s"$${{ secrets.$envKey }}").toMap

inThisBuild(
  List(
    organization := "io.github.masonedmison",
    licenses := List("Apache-2.0" -> new URL("http://www.apache.org/licenses/LICENSE-2.0")),
    startYear := Some(2022),
    sonatypeCredentialHost := "s01.oss.sonatype.org",
    sonatypeRepository := "https://s01.oss.sonatype.org/service/local",
    developers := List(
      Developer(
        "mason.edmison",
        "Mason Lazalier Edmison",
        "mason.edmison@gmail.com",
        url("https://github.com/masonedmison")
      )
    ),
    versionScheme := Some("early-semver")
  )
)
val Versions = new {
  val shapeless = "2.3.10"
  val cats      = "2.9.0"
}

val commonSettings: Seq[Setting[_]] = Seq(
  scalacOptions -= "-Xfatal-warnings",
  testFrameworks += new TestFramework("munit.Framework")
)

lazy val orderShapeless = (project in file("order-shapeless"))
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "com.chuusai"    %% "shapeless"    % Versions.shapeless,
      "org.typelevel"  %% "cats-core"    % Versions.cats,
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "org.scalameta"  %% "munit"        % "0.7.29" % Test
    )
  )

val root = project
  .in(file("."))
  .settings(publish := {}, publish / skip := true)
  .aggregate(orderShapeless)
