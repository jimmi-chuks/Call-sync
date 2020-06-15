package com.dani.contactsynchttp4s

import cats.effect.{ExitCode, IO, IOApp}
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.http4s.server.blaze.BlazeServerBuilder
import com.dani.contactsynchttp4s.modules._
import com.dani.contactsynchttp4s.modules.shop.modules.{HttpApi, Security}
import cats.implicits._

object Main extends IOApp {

  implicit val logger = Slf4jLogger.getLogger[IO]

  override def run(args: List[String]): IO[ExitCode] =
    config.load[IO].flatMap { cfg =>
      Logger[IO].info(s"Loaded config $cfg") *>
        AppResources.make[IO](cfg).use { res =>
          for {
            security <- Security.make[IO](cfg, res.psql, res.redis)
            algebras <- Algebras.make[IO](res.redis, res.psql)
            api <- HttpApi.make[IO](algebras, security)
            _ <- BlazeServerBuilder[IO]
              .bindHttp(
                cfg.httpServerConfig.port.value,
                cfg.httpServerConfig.host.value
              )
              .withHttpApp(api.httpApp)
              .serve
              .compile
              .drain
          } yield ExitCode.Success
        }
    }

}