package com.dani.contactsynchttp4s.http.routes

import cats._
import cats.implicits._
import com.dani.contactsynchttp4s.algebras.CallActionService
import com.dani.contactsynchttp4s.domain.auth._
import com.dani.contactsynchttp4s.domain.callAction.{CallId, CreateCallActionParam}
import com.dani.contactsynchttp4s.effects._
import com.dani.contactsynchttp4s.http.auth.user.CommonUser
import com.dani.contactsynchttp4s.http.decoder._
import com.dani.contactsynchttp4s.http.json._
import org.http4s._
import org.http4s.circe.JsonDecoder
import org.http4s.dsl.Http4sDsl
import org.http4s.server.{AuthMiddleware, Router}
import com.dani.contactsynchttp4s.algebras.Auth


final class CallActionRoutes[F[_]: Defer: JsonDecoder: MonadThrow](
  callActionService: CallActionService[F]
) extends Http4sDsl[F] {

  private[routes] val prefixPath = "/call"

  private val httpRoutes: AuthedRoutes[CommonUser, F] = AuthedRoutes.of {

    case ar @ POST -> Root as user=>
      ar.req.decodeR[CreateCallActionParam] { callActionParam =>
        callActionService
          .saveCallAction(callActionParam.toDomain)
          .flatMap(Created(_))
      }

    case GET -> Root as user =>
      Ok(callActionService.findAllUserCallActions(user.value.userId))

    case GET -> Root / UUIDVar(callId) as user =>
      Ok(callActionService.findCallByUserAndCallId(user.value.userId, CallId(callId)))


//    case GET -> Root :? CallQueryParam(contact) as user =>
//      Ok(
//        contact.fold(callActionService.findAllUserCallActions(user.value.userId))(
//          c => callActionService.findCallByUserAndCallId(user.value.userId, c)
//        )
//      )
  }

  def routes(authMiddleware: AuthMiddleware[F, CommonUser]): HttpRoutes[F] = Router(
    prefixPath -> authMiddleware(httpRoutes)
  )

}
