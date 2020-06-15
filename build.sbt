val Http4sVersion = "0.21.0"
val CirceVersion = "0.13.0"
val CirceConfigVersion = "0.6.1"
val CirceEnumeratumVersion = "1.6.1"
val Specs2Version = "4.8.3"
val LogbackVersion = "1.2.3"
val RefinedType = "0.9.13"
val KindProjector = "0.10.3"
val Http4sJwtAuth = "0.0.4"
val BetterMonadic = "0.3.0"
val Redis4CatsEffects = "0.9.3"
val NewType = "0.4.3"
val catsMeowMtl   = "0.4.0"
val Squants = "1.6.0"
val CatsRetryVersion = "1.1.0"
val Log4cats      = "1.0.1"
val Redis4cats    = "0.9.3"
val Skunk         = "0.0.7"
val Ciris = "1.0.4"
val kindProjector    = "0.11.0"

lazy val root = (project in file("."))
  .settings(
    organization := "com.dani",
    name := "contact-sync-http4s",
    version := "0.0.1-SNAPSHOT",
    resolvers += Resolver.sonatypeRepo("snapshots"),
    scalaVersion := "2.13.1",
    scalacOptions += "-Ymacro-annotations",
    libraryDependencies ++= Seq(
      "org.http4s"          %% "http4s-blaze-server"    % Http4sVersion,
      "org.http4s"          %% "http4s-blaze-client"    % Http4sVersion,
      "org.http4s"          %% "http4s-circe"           % Http4sVersion,
      "org.http4s"          %% "http4s-dsl"             % Http4sVersion,
      "io.circe"            %% "circe-generic"          % CirceVersion,
      "io.circe"            %% "circe-core"             % CirceVersion,
      "com.beachape"        %% "enumeratum-circe"       % CirceEnumeratumVersion,
      "io.circe"            %% "circe-parser"           % CirceVersion,
      "io.circe"            %% "circe-refined"          % CirceVersion,
      "io.circe"            %% "circe-shapes"           % CirceVersion,
      "org.specs2"          %% "specs2-core"            % Specs2Version % "test",
      "ch.qos.logback"      %  "logback-classic"        % LogbackVersion,
      "eu.timepit"          %% "refined"                % RefinedType,
      "eu.timepit"          %% "refined-cats"           % RefinedType,
      "dev.profunktor"      %% "http4s-jwt-auth"        % Http4sJwtAuth,
      "dev.profunktor"      %% "redis4cats-effects"     % Redis4CatsEffects,
      "io.estatico"         %% "newtype"                % NewType,
      "org.typelevel"       %% "squants"                % Squants,
      "com.github.cb372"    %% "cats-retry"             % CatsRetryVersion,
      "io.chrisdavenport"   %% "log4cats-slf4j"         % Log4cats,
      "dev.profunktor"      %% "redis4cats-effects"     % Redis4cats,
      "dev.profunktor"      %% "redis4cats-log4cats"    % Redis4cats,
      "org.tpolecat"        %% "skunk-core"             % Skunk,
      "org.tpolecat"        %% "skunk-circe"            % Skunk,
      "is.cir"              %% "ciris"                  % Ciris,
      "is.cir"              %% "ciris-enumeratum"       % Ciris,
      "is.cir"              %% "ciris-refined"          % Ciris,
      "com.olegpy"       %% "meow-mtl-core" % catsMeowMtl
    ),
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
  )
