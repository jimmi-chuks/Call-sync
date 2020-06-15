package com.dani.contactsynchttp4s.modules

import cats.Parallel
import cats.effect._
import cats.implicits._
import com.dani.contactsynchttp4s.algebras.{
  CallActionService,
  LiveCallActionService
}
import dev.profunktor.redis4cats.algebra.RedisCommands
import skunk._

object Services {
  def make[F[_]: Concurrent: Parallel: Timer](
    redis: RedisCommands[F, String, String],
    sessionPool: Resource[F, Session[F]]
  ): F[Services[F]] =
    for {
      callActions <- LiveCallActionService.make[F](sessionPool)
    } yield new Services[F](callActions)
}

class Services[F[_]] private (val callAction: CallActionService[F])
