package com.dani.contactsynchttp4s.http

import cats.implicits._
import io.circe.Decoder
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import com.dani.contactsynchttp4s.effects._

object decoder {

  implicit class RefinedRequestDecoder[F[_]: JsonDecoder: MonadThrow](req: Request[F]) extends Http4sDsl[F] {

    def decodeR[A: Decoder](f: A => F[Response[F]]): F[Response[F]] =
      req.asJsonDecode[A].attempt.flatMap {
        case Left(e) =>
          Option(e.getCause) match {
            case Some(c) if c.getMessage.startsWith("Predicate") => BadRequest(c.getMessage)
            case _                                               => UnprocessableEntity()
          }
        case Right(a) => f(a)
      }

  }

}
