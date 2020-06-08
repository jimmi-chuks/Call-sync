package com.dani.contactsynchttp4s.algebras

import cats.effect.{Resource, Sync}
import com.dani.contactsynchttp4s.domain.auth.UserId
import com.dani.contactsynchttp4s.domain.callAction._
import com.dani.contactsynchttp4s.domain.contact.PhoneNumber
import com.dani.contactsynchttp4s.effects.{BracketThrow, GenUUID}
import com.dani.contactsynchttp4s.enums.CallType
import com.dani.contactsynchttp4s.ext.skunkx._
import skunk.codec.all.{int8, uuid, varchar}
import skunk.implicits._
import cats.implicits._
import skunk.{Command, Decoder, Encoder, Query, Session, ~}

trait CallActionService[F[_]] {
  def saveCallAction(createCallAction: CreateCallAction): F[Unit]
  def findAllUserCallActions(userId: UserId): F[List[CallAction]]
  def findCallByUserAndCallId(userId: UserId, callId: CallId): F[Option[CallAction]]
}

object LiveCallActionService {
  def make[F[_]: Sync](
    sessionPool: Resource[F, Session[F]]
  ): F[CallActionService[F]] =
    Sync[F].delay(new LiveCallActionService[F](sessionPool))
}

final class LiveCallActionService[F[_]: BracketThrow: GenUUID] private (
  sessionPool: Resource[F, Session[F]]
) extends CallActionService[F] {

  import CallQueries._

  override def saveCallAction(createCallAction: CreateCallAction): F[Unit] =
    sessionPool.use { session =>
      session.prepare(insertCallAction).use { cmd =>
        GenUUID[F].make[CallId].flatMap { id =>
          cmd
            .execute( id ~ createCallAction).as(id)
        }
      }
    }

  override def findAllUserCallActions(userId: UserId): F[List[CallAction]] =
    sessionPool.use { session =>
      session.prepare(selectAllByCaller).use { q =>
        q.stream(userId, chunkSize = 1024).compile.toList
      }
    }

  override def findCallByUserAndCallId(userId: UserId, callId: CallId): F[Option[CallAction]] =
    sessionPool.use{ session =>
      session.prepare(selectCallByCallerAndCallId).use{ q =>
        q.option(callId, userId)
      }
    }

}

private object CallQueries {

//  val myenum = enum[CallType](_.label, CallType.fromLabel, Type("calltype"))

  val decoder: Decoder[CallAction] =
    (uuid ~ uuid ~ varchar ~ varchar ~ int8 ~ varchar ~ int8).map {
      case id ~ _ ~ ct ~ pn ~ ti ~ di ~ cd =>
        CallAction(
          CallId(id),
          CallType.fromLabel(ct),
          PhoneNumber(pn),
          CallTime(ti),
          DeviceInfo(di),
          CallDuration(cd)
        )
    }

  val encoder: Encoder[CallId ~ CreateCallAction] =
    (
      uuid.cimap[CallId] ~
        varchar.cimap[CallType] ~
        varchar.cimap[PhoneNumber] ~
        int8.cimap[CallTime] ~
        varchar.cimap[DeviceInfo] ~
        int8.cimap[CallDuration]
    ).contramap {
      case cid ~ c =>
        cid ~ c.callType ~ c.phoneNumber ~ c.callTime ~ c.deviceInfo ~ c.callDuration
    }

  val selectAllByCaller: Query[UserId, CallAction] =
    sql"""
          SELECT * FROM calls
          WHERE caller_id = ${varchar.cimap[UserId]}
         """.query(decoder)

  val selectCallByCallerAndCallId: Query[CallId ~ UserId, CallAction] =
    sql"""
          SELECT * FROM calls
          WHERE uuid = ${varchar.cimap[CallId]}
          AND caller_id = ${varchar.cimap[UserId]}
         """.query(decoder)

  val insertCallAction: Command[CallId ~ CreateCallAction] =
    sql"""
        INSERT INTO contacts
        VALUES ($encoder)
       """.command

}
