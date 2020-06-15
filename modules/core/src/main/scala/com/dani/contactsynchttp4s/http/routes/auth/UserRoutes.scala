package com.dani.contactsynchttp4s.http.routes.auth

import cats._
import cats.implicits._
import com.dani.contactsynchttp4s.algebras.Auth
import com.dani.contactsynchttp4s.domain.auth.{AccountAlreadyCreated, CreateUser}
import com.dani.contactsynchttp4s.effects.MonadThrow
import org.http4s._
import org.http4s.circe.JsonDecoder
import com.dani.contactsynchttp4s.http.json._
import com.dani.contactsynchttp4s.http.decoder._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

final class UserRoutes[F[_]: Defer: JsonDecoder: MonadThrow](
    auth: Auth[F]
) extends Http4sDsl[F] {

  private[routes] val prefixPath = "/auth"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {

    case req @ POST -> Root / "users" =>
      req
        .decodeR[CreateUser] { user =>
          auth
            .newUser(user.signUpId.toDomain, user.signUpType, user.userName.toDomain)
            .flatMap(Created(_))
            .recoverWith {
              case AccountAlreadyCreated(s, _) => Conflict(s.value)
            }
        }

  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )

}
