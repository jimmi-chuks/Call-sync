package com.dani.contactsynchttp4s.algebras

import cats.effect.Sync
import cats.implicits._
import com.dani.contactsynchttp4s.config.data.{JwtSecretKeyConfig, TokenExpiration}
import com.dani.contactsynchttp4s.effects.GenUUID
import dev.profunktor.auth.jwt._
import io.circe.syntax._
import pdi.jwt._

import scala.concurrent.duration.FiniteDuration

trait TokenService[F[_]] {
  def create: F[JwtToken]
}

object LiveTokenService {
  def make[F[_]: Sync](
      tokenConfig: JwtSecretKeyConfig,
      tokenExpiration: TokenExpiration
  ): F[TokenService[F]] =
    Sync[F].delay(java.time.Clock.systemUTC).map { implicit jClock =>
      new LiveTokenService[F](tokenConfig, tokenExpiration.value)
    }
}

final class LiveTokenService[F[_]: GenUUID: Sync] private(
    config: JwtSecretKeyConfig,
    exp: FiniteDuration
)(implicit val ev: java.time.Clock)
    extends TokenService[F] {
  def create: F[JwtToken] =
    for {
      uuid <- GenUUID[F].make
      claim <- Sync[F].delay(JwtClaim(uuid.asJson.noSpaces).issuedNow.expiresIn(exp.toMillis))
      secretKey = JwtSecretKey(config.value.value.value)
      token <- jwtEncode[F](claim, secretKey, JwtAlgorithm.HS256)
    } yield token
}
