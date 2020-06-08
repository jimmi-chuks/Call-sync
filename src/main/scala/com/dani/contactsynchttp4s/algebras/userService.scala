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
    sessionPool: Resource[F, Session[F]]
  ): F[UserService[F]] =
    Sync[F].delay(new LiveUserService[F](sessionPool))
}

final class LiveUserService[F[_]: BracketThrow: GenUUID] private (
  sessionPool: Resource[F, Session[F]]
) extends UserService[F] {

  import UserQueries._

  override def createUser(signUpId: SignUpId,
                          signUpType: SignUpType,
                          userName: UserName): F[UserId] =
    sessionPool.use { session =>
      session.prepare(insertUser).use { cmd =>
        GenUUID[F].make[UserId].flatMap { id =>
          cmd
            .execute(User(id, userName, signUpType))
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

  val codec: Codec[User ~ EncryptedSignUpId] =
    (uuid.cimap[UserId] ~
      varchar.cimap[UserName] ~
      varchar.cimap[SignUpType] ~
      varchar.cimap[EncryptedSignUpId]).imap {
      case i ~ u ~ n ~ s => User(i, u, n) ~ s
    } {
      case u ~ s => u.userId ~ u.name ~ u.signUpType ~ s
    }

  val selectUser: Query[SignUpId ~ SignUpType, User] =
    sql"""
        SELECT * FROM users
        WHERE sign_up_id = ${varchar.cimap[SignUpId]}
        AND sign_up_id = ${uuid.cimap[SignUpType]}
       """.query(codec)

  val insertUser: Command[User] =
    sql"""
        INSERT INTO users
        VALUES ($codec)
        """.command

}
