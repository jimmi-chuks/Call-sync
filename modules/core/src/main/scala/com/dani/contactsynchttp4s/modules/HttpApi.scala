package com.dani.contactsynchttp4s.modules

package shop.modules

import cats.effect._
import cats.implicits._
import com.dani.contactsynchttp4s.http.auth.user.{ AdminUser, CommonUser }
import com.dani.contactsynchttp4s.http.routes.auth.{ LoginRoutes, LogoutRoutes, UserRoutes }
import com.dani.contactsynchttp4s.http.routes.{ version, CallActionRoutes, ContactRoutes, HealthRoutes }
import dev.profunktor.auth.JwtAuthMiddleware
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.middleware._

import scala.concurrent.duration._
object HttpApi {
  def make[F[_]: Concurrent: Timer](
      algebras: Algebras[F],
      security: Security[F]
  ): F[HttpApi[F]] =
    Sync[F].delay(
      new HttpApi[F](
        algebras,
        security
      )
    )
}

final class HttpApi[F[_]: Concurrent: Timer] private (
    algebras: Algebras[F],
    security: Security[F]
) {
  private val adminMiddleware =
    JwtAuthMiddleware[F, AdminUser](security.adminJwtAuth.value, security.adminAuth.find)
  private val usersMiddleware =
    JwtAuthMiddleware[F, CommonUser](security.userJwtAuth.value, security.usersAuth.find)

  // Auth routes
  private val loginRoutes  = new LoginRoutes[F](security.auth).routes
  private val logoutRoutes = new LogoutRoutes[F](security.auth).routes(usersMiddleware)
  private val userRoutes   = new UserRoutes[F](security.auth).routes

  // Open routes
  private val healthRoutes = new HealthRoutes[F](algebras.healthCheck).routes

  // Secured routes
  private val callActionRoutes = new CallActionRoutes[F](algebras.callActionService).routes(usersMiddleware)
  private val contactRoutes    = new ContactRoutes[F](algebras.contactService).routes(usersMiddleware)

  // Combining all the http routes
  private val openRoutes: HttpRoutes[F] =
    healthRoutes <+> loginRoutes <+> userRoutes <+>
        logoutRoutes <+> callActionRoutes <+> contactRoutes

  private val routes: HttpRoutes[F] = Router(version.v1 -> openRoutes)

  private val middleware: HttpRoutes[F] => HttpRoutes[F] = {
    { http: HttpRoutes[F] =>
      AutoSlash(http)
    } andThen { http: HttpRoutes[F] =>
      CORS(http, CORS.DefaultCORSConfig)
    } andThen { http: HttpRoutes[F] =>
      Timeout(60.seconds)(http)
    }
  }

  private val loggers: HttpApp[F] => HttpApp[F] = {
    { http: HttpApp[F] =>
      RequestLogger.httpApp(true, true)(http)
    } andThen { http: HttpApp[F] =>
      ResponseLogger.httpApp(true, true)(http)
    }
  }

  val httpApp: HttpApp[F] = loggers(middleware(routes).orNotFound)

}
