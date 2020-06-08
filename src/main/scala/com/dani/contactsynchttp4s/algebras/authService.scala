package com.dani.contactsynchttp4s.algebras

import cats.effect.Sync
import cats.implicits._
import cats.{ Applicative, Functor }
import com.dani.contactsynchttp4s.config.data.TokenExpiration
import com.dani.contactsynchttp4s.domain.auth._
import com.dani.contactsynchttp4s.effects.{ GenUUID, MonadThrow }
import com.dani.contactsynchttp4s.enums.{ MyEnum, SignUpType }
import com.dani.contactsynchttp4s.http.auth.user._
import com.dani.contactsynchttp4s.http.json._
import dev.profunktor.auth.jwt.JwtToken
import dev.profunktor.redis4cats.algebra.RedisCommands
import io.circe.parser.decode
import io.circe.syntax._
import pdi.jwt.JwtClaim
import skunk.codec.enum._
import skunk.data.Type

trait Auth[F[_]] {
  def newUser(signUpId: SignUpId, signUpType: SignUpType, name: UserName): F[JwtToken]
  def login(signUpId: SignUpId, signUpType: SignUpType): F[JwtToken]
  def logout(token: JwtToken, signUpId: UserName): F[Unit]
}

trait UserServiceAuth[F[_], A] {
  def find(token: JwtToken)(claim: JwtClaim): F[Option[A]]
}

object LiveAdminAuth {
  def make[F[_]: Sync](adminToken: JwtToken, adminUser: AdminUser): F[UserServiceAuth[F, AdminUser]] =
    Sync[F].delay(new LiveAdminAuth(adminToken, adminUser))
}

class LiveAdminAuth[F[_]: Applicative](adminToken: JwtToken, adminUser: AdminUser)
    extends UserServiceAuth[F, AdminUser] {

  def find(token: JwtToken)(claim: JwtClaim): F[Option[AdminUser]] =
    (token == adminToken)
      .guard[Option]
      .as(adminUser)
      .pure[F]

}

object LiveUserServiceAuth {
  def make[F[_]: Sync](
      redis: RedisCommands[F, String, String]
  ): F[UserServiceAuth[F, CommonUser]] =
    Sync[F].delay(new LiveUserServiceAuth(redis))
}

class LiveUserServiceAuth[F[_]: Functor](
    redis: RedisCommands[F, String, String]
) extends UserServiceAuth[F, CommonUser] {

  def find(token: JwtToken)(claim: JwtClaim): F[Option[CommonUser]] =
    redis
      .get(token.value)
      .map(_.flatMap { u =>
        decode[User](u).toOption.map(CommonUser.apply)
      })

}

object LiveAuth {
  def make[F[_]: Sync](
      tokenExpiration: TokenExpiration,
      tokens: TokenService[F],
      users: UserService[F],
      redis: RedisCommands[F, String, String]
  ): F[Auth[F]] =
    Sync[F].delay(new LiveAuth(tokenExpiration, tokens, users, redis))
}

final class LiveAuth[F[_]: GenUUID: MonadThrow] private (
    tokenExpiration: TokenExpiration,
    tokens: TokenService[F],
    users: UserService[F],
    redis: RedisCommands[F, String, String]
) extends Auth[F] {

  private val TokenExpiration = tokenExpiration.value

  // A codec that maps Postgres type `myenum` to Scala type `MyEnum`
  val myenum = enum(MyEnum, Type("myenum"))

  def newUser(signUpId: SignUpId, signUpType: SignUpType, name: UserName): F[JwtToken] =
    users.find(signUpId, signUpType).flatMap {
      case Some(_) => AccountAlreadyCreated(signUpId, name).raiseError[F, JwtToken]
      case None =>
        for {
          i <- users.createUser(signUpId, signUpType, name)
          t <- tokens.create
          u = User(i, name, signUpType).asJson.noSpaces
          _ <- redis.setEx(t.value, u, TokenExpiration)
          _ <- redis.setEx(signUpId.value, t.value, TokenExpiration)
        } yield t
    }

  def login(signUpId: SignUpId, signUpType: SignUpType): F[JwtToken] =
    users.find(signUpId, signUpType).flatMap {
      case None =>
        signUpType match {
          case SignUpType.GoogleAccount   => InvalidGoogleIdId(signUpId).raiseError[F, JwtToken]
          case SignUpType.FacebookAccount => InvalidFacebookId(signUpId).raiseError[F, JwtToken]
        }
      case Some(user) =>
        redis.get(signUpId.value).flatMap {
          case Some(t) => JwtToken(t).pure[F]
          case None =>
            tokens.create.flatTap { t =>
              redis.setEx(t.value, user.asJson.noSpaces, TokenExpiration) *>
                redis.setEx(signUpId.value, t.value, TokenExpiration)
            }
        }
    }

  def logout(token: JwtToken, signUpId: UserName): F[Unit] =
    redis.del(token.value) *> redis.del(signUpId.value)

}
