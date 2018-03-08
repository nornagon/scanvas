lazy val commonSettings = Seq(
  organization := "net.nornagon",
  scalaVersion := "2.12.2",
  version := "0.1.0-SNAPSHOT",
  resolvers += Resolver.mavenLocal
)

publish := {}
publishLocal := {}

lazy val core = (project in file("core"))
  .settings(
    commonSettings,
    name := "scanvas-core",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.0.1" % Test,
      "org.bytedeco.javacpp-presets" % "skia" % "20170511-53d6729-1.3",
      "org.bytedeco.javacpp-presets" % "skia" % "20170511-53d6729-1.3" classifier "macosx-x86_64"
    )
  )

lazy val gpu = (project in file("gpu"))
  .settings(
    commonSettings,
    name := "scanvas-gpu",
    libraryDependencies ++= Seq(
      "org.lwjgl" % "lwjgl-opengl" % "3.1.1",
      "org.lwjgl" % "lwjgl-opengl" % "3.1.1" classifier "natives-macos" classifier "natives-linux",
      "org.lwjgl" % "lwjgl-glfw" % "3.1.1",
      "org.lwjgl" % "lwjgl-glfw" % "3.1.1" classifier "natives-macos" classifier "natives-linux",
      "org.lwjgl" % "lwjgl" % "3.1.1",
      "org.lwjgl" % "lwjgl" % "3.1.1" classifier "natives-macos" classifier "natives-linux"
    )
  )
  .dependsOn(core)
