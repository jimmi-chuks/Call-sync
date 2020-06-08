package com.dani.contactsynchttp4s.http.routes.auth

import cats._
import cats.implicits._
import com.dani.contactsynchttp4s.algebras.Auth
import com.dani.contactsynchttp4s.domain.auth.{InvalidFacebookId, InvalidGoogleIdId, InvalidUserOrPassword, LoginUser}
import com.dani.contactsynchttp4s.effects.MonadThrow
import org.http4s._
import org.http4s.circe.JsonDecoder
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import com.dani.contactsynchttp4s.http.json._
import com.dani.contactsynchttp4s.http.decoder._

final class LoginRoutes[F[_]: Defer: JsonDecoder: MonadThrow](
    auth: Auth[F]
) extends Http4sDsl[F] {

  private[routes] val prefixPath = "/auth"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {

    case req @ POST -> Root / "login" =>
      req.decodeR[LoginUser] { user =>
        auth
          .login(user.signUpIdParam.toDomain, user.signUpType)
          .flatMap(Ok(_))
          .recoverWith {
            case InvalidFacebookId(_) | InvalidGoogleIdId(_) => Forbidden()
          }
      }
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )

}
