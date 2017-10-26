import sbt._

object Dependencies {
  val akkaV      = "2.4.19"
  val akkaHttpV  = "10.0.10"
  val circeV     = "0.8.0"
  val akkaCirceV = "1.18.0"
  val scalaTestV = "3.0.1"
  val logbackV   = "1.1.8"
  val configV    = "1.3.1"
  val timeV      = "2.16.0"
  val monixV     = "2.3.0"
  val pureConfV  = "0.7.2"


  lazy val projectResolvers = Seq.empty
  lazy val dependencies = testDependencies ++ rootDependencies


  lazy val testDependencies = Seq (
    "org.scalatest"          %% "scalatest"             % scalaTestV % Test,
    "com.typesafe.akka"      %% "akka-http-testkit"     % akkaHttpV  % Test
  )

  lazy val rootDependencies = Seq(
    "com.typesafe.akka"      %% "akka-http"             % akkaHttpV,
    "de.heikoseeberger"      %% "akka-http-circe"       % akkaCirceV,
    "io.monix"               %% "monix"                 % monixV,
    "io.monix"               %% "monix-cats"            % monixV,
    "io.circe"               %% "circe-core"            % circeV,
    "io.circe"               %% "circe-generic"         % circeV,
    "io.circe"               %% "circe-parser"          % circeV,
    "com.github.nscala-time" %% "nscala-time"           % timeV,
    "com.typesafe"            % "config"                % configV,
    "com.typesafe.akka"      %% "akka-slf4j"            % akkaV,
    "ch.qos.logback"          % "logback-classic"       % logbackV,
    "com.github.pureconfig"  %% "pureconfig"            % pureConfV
  )
}
