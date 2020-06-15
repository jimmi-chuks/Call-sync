package com.dani.contactsynchttp4s.modules

import cats.Parallel
import cats.effect._
import cats.implicits._
import com.dani.contactsynchttp4s.algebras._
import dev.profunktor.redis4cats.algebra.RedisCommands
import skunk._

object Algebras {
  def make[F[_]: Concurrent: Parallel: Timer](
      redis: RedisCommands[F, String, String],
      sessionPool: Resource[F, Session[F]]
  ): F[Algebras[F]] =
    for {
      contactService <- LiveContactService.make[F](sessionPool)
      callActionService <- LiveCallActionService.make[F](sessionPool)
      healthCheck <- LiveHealthCheck.make[F](sessionPool, redis)
    } yield new Algebras[F](
      contactService,
      callActionService,
      healthCheck
    )
}

final class Algebras[F[_]] private (
    val contactService: ContactService[F],
    val callActionService: CallActionService[F],
    val healthCheck: HealthCheck[F]
)
