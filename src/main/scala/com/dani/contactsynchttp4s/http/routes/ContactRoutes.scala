package com.dani.contactsynchttp4s.http.routes

import cats._
import cats.implicits._
import com.dani.contactsynchttp4s.algebras.ContactService
import com.dani.contactsynchttp4s.domain.auth.UserId
import com.dani.contactsynchttp4s.domain.contact.{ContactId, CreateContact, CreateContactParam}
import com.dani.contactsynchttp4s.effects.MonadThrow
import com.dani.contactsynchttp4s.http.auth.user.CommonUser
import com.dani.contactsynchttp4s.http.decoder._
import com.dani.contactsynchttp4s.http.json._
import org.http4s._
import org.http4s.circe.JsonDecoder
import org.http4s.dsl.Http4sDsl
import org.http4s.server.{AuthMiddleware, Router}

final class ContactRoutes[F[_]: Defer: JsonDecoder: MonadThrow](
    contactService: ContactService[F]
) extends Http4sDsl[F] {

  private[routes] val prefixPath = "/contact"

  object ContactQueryParam extends QueryParamDecoderMatcher[ContactId]("contact")

  private val httpRoutes: AuthedRoutes[CommonUser, F] = AuthedRoutes.of {

    case ar @ POST -> Root as user =>
      ar.req.decodeR[CreateContactParam] { contactParam =>
        contactService
          .saveContact(contactParam.toDomain(user.value.userId))
          .flatMap(Created(_))
      }

    case GET -> Root as user  =>
      Ok(contactService.getContactsBy(user.value.userId))

    case GET -> Root :? ContactQueryParam(contact) as user =>
      Ok(contactService.getContactBy(user.value.userId, contact))
  }

  def routes(authMiddleware: AuthMiddleware[F, CommonUser]): HttpRoutes[F] = Router(
    prefixPath -> authMiddleware(httpRoutes)
  )

}
