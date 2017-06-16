import com.typesafe.sbt.SbtMultiJvm.multiJvmSettings
import com.typesafe.sbt.SbtMultiJvm.MultiJvmKeys.MultiJvm
import ByteConversions._

val akkaVersion = "2.5.1"

lazy val `akka-sample-cluster-scala` = project
  .in(file("."))
  /**
    * Required for ConductR bundle: sbt-native-packager JavaAppPackaging
    */
  .enablePlugins(JavaAppPackaging)
  .settings(multiJvmSettings: _*)
  .settings(
    organization := "com.typesafe.akka.samples",
    scalaVersion := "2.12.2",
    scalacOptions in Compile ++= Seq("-deprecation", "-feature", "-unchecked", "-Xlog-reflective-calls", "-Xlint"),
    javacOptions in Compile ++= Seq("-Xlint:unchecked", "-Xlint:deprecation"),
    javaOptions in run ++= Seq("-Djava.library.path=./target/native"),
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-remote" % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster-metrics" % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion,
      "com.typesafe.akka" %% "akka-multi-node-testkit" % akkaVersion,
      "com.typesafe.conductr" %% "akka24-conductr-bundle-lib" % "1.9.0",
      "org.scalatest" %% "scalatest" % "3.0.1" % Test,
      "io.kamon" % "sigar-loader" % "1.6.6-rev002"),
    fork in run := true,
    /**
      * This project has multiple main's.  To generate the start script for sbt-native-packager it's required to specify
      * a main class in the Compile scope.
      *
      * When multiple scopes are used it doesn't work, why?
      * i.e. // mainClass in (Compile, run) := Some("sample.cluster.simple.SimpleClusterApp"),
      */
    mainClass in Compile := Some("sample.cluster.simple.SimpleConductRClusterApp"),
    // disable parallel tests
    parallelExecution in Test := false,
    licenses := Seq(("CC0", url("http://creativecommons.org/publicdomain/zero/1.0"))),

    javaOptions in Universal ++= Seq(
      "-J-Xmx2g",
      "-J-Xms2g"
    ),
    /**
      * ConductR bundle configuration
      */
    BundleKeys.system := "ClusterSystem",
    BundleKeys.roles := Set("akka-cluster"),
    BundleKeys.endpoints := Map("akka-remote" -> Endpoint("tcp")),

    BundleKeys.nrOfCpus := 2.0,
    BundleKeys.memory := 2.GB,
    BundleKeys.diskSpace := 100.MB
  )
  .configs (MultiJvm)
