package com.dani.contactsynchttp4s.algebras

import cats.effect.{Resource, Sync}
import cats.implicits._
import com.dani.contactsynchttp4s.domain.auth._
import com.dani.contactsynchttp4s.effects.{BracketThrow, GenUUID}
import com.dani.contactsynchttp4s.enums.SignUpType
import com.dani.contactsynchttp4s.ext.skunkx._
import com.dani.contactsynchttp4s.http.auth.user.User
import skunk._
import skunk.codec.all._
import skunk.implicits._

trait UserService[F[_]] {
  def createUser(signUpId: SignUpId,
                 signUpType: SignUpType,
                 userName: UserName): F[UserId]
  def find(signUpId: SignUpId, signUpType: SignUpType): F[Option[User]]
}

object LiveUserService {
  def make[F[_]: Sync](
    sessionPool: Resource[F, Session[F]],
    crypto: Crypto
  ): F[UserService[F]] =
    Sync[F].delay(new LiveUserService[F](sessionPool, crypto))
}

final class LiveUserService[F[_]: BracketThrow: GenUUID] private (
  sessionPool: Resource[F, Session[F]],
  crypto: Crypto
) extends UserService[F] {

  import UserQueries._

  override def createUser(signUpId: SignUpId,
                          signUpType: SignUpType,
                          userName: UserName): F[UserId] =
    sessionPool.use { session =>
      session.prepare(insertUser).use { cmd =>
        GenUUID[F].make[UserId].flatMap { id =>
          cmd
            .execute(User(id, userName, signUpType) ~ crypto.encrypt(signUpId))
            .as(id)
            .handleErrorWith {
              case SqlState.UniqueViolation(_) => {
                AccountAlreadyCreated(signUpId, userName).raiseError[F, UserId]
              }
            }
        }
      }

    }

  override def find(signUpId: SignUpId,
                    signUpType: SignUpType): F[Option[User]] =
    sessionPool.use { session =>
      session.prepare(selectUser).use { q =>
        q.option(signUpId, signUpType)
      }
    }
}

private object UserQueries {
  import com.dani.contactsynchttp4s.enums.SignUpType.signUpTypeCodec

  val decoder: Decoder[User] =
    (uuid ~ varchar ~ signUpTypeCodec ~ varchar).map {
      case i ~ u ~ st ~ _ => User(UserId(i), UserName(u), st)
    }

  val encoder: Encoder[User ~ EncryptedSignUpId] =
    (
      uuid.cimap[UserId] ~
        varchar.cimap[UserName] ~
        signUpTypeCodec.asEncoder ~
        varchar.cimap[EncryptedSignUpId]
      ).contramap {
      case u ~ sid =>
        u.userId ~ u.name ~ u.signUpType ~ sid
    }

  val selectUser: Query[SignUpId ~ SignUpType, User] =
    sql"""
        SELECT * FROM users
        WHERE sign_up_id = ${varchar.cimap[SignUpId]}
        AND sign_up_id = $signUpTypeCodec
       """.query(decoder)

  val insertUser: Command[User ~ EncryptedSignUpId]  =
    sql"""
        INSERT INTO users
        VALUES ($encoder)
        """.command

}
