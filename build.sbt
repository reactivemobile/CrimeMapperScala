import com.github.retronym.SbtOneJar._

name := "CrimeMapperSandbox"

oneJarSettings

version := "1.0"

scalaVersion := "2.12.1"

libraryDependencies +=  "org.scalaj" %% "scalaj-http" % "2.3.0"
libraryDependencies += "net.liftweb" %% "lift-json" % "3.1.0-M1"