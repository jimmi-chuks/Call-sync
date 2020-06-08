package com.dani.contactsynchttp4s.algebras

import cats.effect.{Resource, Sync}
import com.dani.contactsynchttp4s.domain.auth.UserId
import com.dani.contactsynchttp4s.domain.contact._
import com.dani.contactsynchttp4s.effects.{BracketThrow, GenUUID}
import com.dani.contactsynchttp4s.ext.skunkx._
import skunk.codec.all.{uuid, varchar}
import skunk.implicits._
import cats.implicits._
import skunk.{Command, Decoder, Query, Session, _}

trait ContactService[F[_]] {
  def getContactsBy(callerId: UserId): F[List[Contact]]
  def getContactBy(ownerId: UserId, contactId: ContactId): F[Option[Contact]]
  def saveContact(createContact: CreateContact): F[Unit]
}

object LiveContactService {
  def make[F[_]: Sync](
    sessionPool: Resource[F, Session[F]]
  ): F[ContactService[F]] =
    Sync[F].delay(new LiveContactService[F](sessionPool))
}

final class LiveContactService[F[_]: BracketThrow: GenUUID] private (
  sessionPool: Resource[F, Session[F]]
) extends ContactService[F] {
  import ContactQueries._

  override def getContactsBy(ownerId: UserId): F[List[Contact]] =
    sessionPool.use { session =>
      session.prepare(selectAllByOwner).use { q =>
        q.stream(ownerId, chunkSize = 1024).compile.toList
      }
    }

  override def getContactBy(ownerId: UserId, contactId: ContactId): F[Option[Contact]] =
    sessionPool.use { session =>
      session.prepare(selectByUserIdAndContactId).use { q =>
        q.option(ownerId, contactId)
      }
    }

  override def saveContact(createContact: CreateContact): F[Unit] =
    sessionPool.use { session =>
      session.prepare(insertContact).use { cmd =>
        GenUUID[F].make[ContactId].flatMap { id =>
          cmd
            .execute(id ~ createContact).as(id)
        }
      }
    }
}

private object ContactQueries {

  val decoder: Decoder[Contact] =
    (uuid.cimap[ContactId] ~ varchar.cimap[PhoneNumber] ~ uuid ~ varchar
      .cimap[SavedName]).map {
      case c ~ p ~ _ ~ s => Contact(c, p, s)
    }

  val encoder: Encoder[ContactId ~ CreateContact] =
    (
      uuid.cimap[ContactId] ~
        varchar.cimap[PhoneNumber] ~
        varchar.cimap[SavedName] ~
        uuid.cimap[UserId]
    ).contramap {
      case id ~ c =>
        id ~ c.phoneNumber ~ c.savedName ~ c.ownerId
    }

  val selectAllByOwner: Query[UserId, Contact] =
    sql""""
          SELECT * FROM contacts
          WHERE owner_id = ${varchar.cimap[UserId]}
    """.query(decoder)

  val selectByUserIdAndContactId: Query[UserId ~ ContactId, Contact] =
    sql"""
        SELECT * FROM contacts
        WHERE owner_id = ${uuid.cimap[UserId]}
        AND uuid = ${uuid.cimap[ContactId]}
       """.query(decoder)

  val insertContact: Command[ContactId ~ CreateContact] =
    sql"""
        INSERT INTO contacts
        VALUES ($encoder)
       """.command

////  Insert with inline encoder
//  val insertItem: Command[ContactId ~ CreateContact] =
//    sql"""
//        INSERT INTO contacts
//        VALUES ($uuid, $varchar, $uuid, $varchar)
//       """.command.contramap {
//      case id ~ c =>
//        id.value ~ c.phoneNumber.value ~ c.ownerId.value ~ c.savedName.value
//    }
}
