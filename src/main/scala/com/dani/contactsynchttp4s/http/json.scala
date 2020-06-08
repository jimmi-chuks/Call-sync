package com.dani.contactsynchttp4s.http

import cats.Applicative
import com.dani.contactsynchttp4s.domain.auth.{CreateUser, LoginUser, SignUpId, UserId}
import com.dani.contactsynchttp4s.domain.callAction.{CallAction, CreateCallAction, CreateCallActionParam}
import com.dani.contactsynchttp4s.domain.contact.{Contact, ContactId, CreateContact, CreateContactParam}
import com.dani.contactsynchttp4s.domain.healthcheck.AppStatus
import com.dani.contactsynchttp4s.http.auth.user.User
import dev.profunktor.auth.jwt.JwtToken
import io.circe.generic.semiauto._
import io.circe.{Encoder, _}
import io.estatico.newtype.Coercible
import io.estatico.newtype.ops._
import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf

object json extends JsonCodecs {
  implicit def deriveEntityEncoder[F[_]: Applicative, A: Encoder]: EntityEncoder[F, A] = jsonEncoderOf[F, A]
}

private[http] trait JsonCodecs {

//   ----- Overriding some Coercible codecs ----
  implicit val userIdParamDecoder: Decoder[UserId] =
    Decoder.forProduct1("userId")(UserId.apply)

  implicit val contactIdParamDecoder: Decoder[ContactId] =
    Decoder.forProduct1("contactId")(ContactId.apply)


  // ----- Coercible codecs ----- These coercible codecs make it possible to get derrivation for newtypes
  implicit def coercibleDecoder[A: Coercible[B, *], B: Decoder]: Decoder[A] =
    Decoder[B].map(_.coerce[A])

  implicit def coercibleEncoder[A: Coercible[B, *], B: Encoder]: Encoder[A] =
    Encoder[B].contramap(_.repr.asInstanceOf[B])

  implicit def coercibleKeyDecoder[A: Coercible[B, *], B: KeyDecoder]: KeyDecoder[A] =
    KeyDecoder[B].map(_.coerce[A])

  implicit def coercibleKeyEncoder[A: Coercible[B, *], B: KeyEncoder]: KeyEncoder[A] =
    KeyEncoder[B].contramap[A](_.repr.asInstanceOf[B])

  // ----- Domain codecs -----

  implicit val callActionDecoder: Decoder[CallAction] = deriveDecoder[CallAction]
  implicit val callActionEncoder: Encoder[CallAction] = deriveEncoder[CallAction]

  implicit val contactDecoder: Decoder[Contact] = deriveDecoder[Contact]
  implicit val contactEncoder: Encoder[Contact] = deriveEncoder[Contact]

  implicit val userDecoder: Decoder[User] = deriveDecoder[User]
  implicit val userEncoder: Encoder[User] = deriveEncoder[User]

  implicit val createContactParamDecoder: Decoder[CreateContactParam] = deriveDecoder[CreateContactParam]

  implicit val createCallActionParamDecoder: Decoder[CreateCallActionParam] = deriveDecoder[CreateCallActionParam]


  implicit val tokenEncoder: Encoder[JwtToken] =
    Encoder.forProduct1("access_token")(_.value)

  implicit val emailLoginDecoder: Decoder[SignUpId] = deriveDecoder[SignUpId]

  //TODO Create decoders for other Login and sign up types

  implicit val loginUserDecoder: Decoder[LoginUser] = deriveDecoder[LoginUser]

  implicit val createUserDecoder: Decoder[CreateUser] = deriveDecoder[CreateUser]

  implicit val createContactDecoder: Decoder[CreateContact] = deriveDecoder[CreateContact]

  implicit val createCallActionDecoder: Decoder[CreateCallAction] = deriveDecoder[CreateCallAction]

  implicit val appStatusEncoder: Encoder[AppStatus] = deriveEncoder[AppStatus]
}