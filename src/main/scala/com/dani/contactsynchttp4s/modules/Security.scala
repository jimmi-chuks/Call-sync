package com.dani.contactsynchttp4s.modules

package shop.modules

import cats.effect._
import cats.implicits._
import com.dani.contactsynchttp4s.algebras.{Auth, LiveAdminAuth, LiveAuth, LiveCrypto, LiveTokenService, LiveUserService, LiveUserServiceAuth, UserServiceAuth}
import com.dani.contactsynchttp4s.config.data.AppConfig
import com.dani.contactsynchttp4s.domain.auth.{ClaimContent, UserId, UserName}
import com.dani.contactsynchttp4s.effects.ApThrow
import com.dani.contactsynchttp4s.enums.SignUpType
import com.dani.contactsynchttp4s.http.auth.user.{AdminJwtAuth, AdminUser, CommonUser, User, UserJwtAuth}
import dev.profunktor.auth.jwt._
import dev.profunktor.redis4cats.algebra.RedisCommands
import io.circe.parser.{decode => jsonDecode}
import pdi.jwt._
import skunk.Session

object Security {
  def make[F[_]: Sync](
                        cfg: AppConfig,
                        sessionPool: Resource[F, Session[F]],
                        redis: RedisCommands[F, String, String]
                      ): F[Security[F]] = {

    val adminJwtAuth: AdminJwtAuth =
      AdminJwtAuth(
        JwtAuth
          .hmac(
            cfg.adminJwtConfig.secretKey.value.value.value,
            JwtAlgorithm.HS256
          )
      )

    val userJwtAuth: UserJwtAuth =
      UserJwtAuth(
        JwtAuth
          .hmac(
            cfg.tokenConfig.value.value.value,
            JwtAlgorithm.HS256
          )
      )

    val adminToken = JwtToken(cfg.adminJwtConfig.adminToken.value.value.value)

    for {
      adminClaim <- jwtDecode[F](adminToken, adminJwtAuth.value)
      content <- ApThrow[F].fromEither(jsonDecode[ClaimContent](adminClaim.content))
      adminUser = AdminUser(User(UserId(content.uuid), UserName("admin"), SignUpType.GoogleAccount))
      tokens <- LiveTokenService.make[F](cfg.tokenConfig, cfg.tokenExpiration)
      users <- LiveUserService.make[F](sessionPool)
      auth <- LiveAuth.make[F](cfg.tokenExpiration, tokens, users, redis)
      adminAuth <- LiveAdminAuth.make[F](adminToken, adminUser)
      usersAuth <- LiveUserServiceAuth.make[F](redis)
    } yield new Security[F](auth, adminAuth, usersAuth, adminJwtAuth, userJwtAuth)

  }
}

final class Security[F[_]] private (
                                     val auth: Auth[F],
                                     val adminAuth: UserServiceAuth[F, AdminUser],
                                     val usersAuth: UserServiceAuth[F, CommonUser],
                                     val adminJwtAuth: AdminJwtAuth,
                                     val userJwtAuth: UserJwtAuth
                                   )
