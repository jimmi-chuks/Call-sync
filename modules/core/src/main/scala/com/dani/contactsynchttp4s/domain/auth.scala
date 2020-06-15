package com.dani.contactsynchttp4s.domain

import java.util.UUID

import com.dani.contactsynchttp4s.enums.SignUpType
import eu.timepit.refined.types.string.NonEmptyString
import io.circe._
import io.estatico.newtype.macros.newtype
import javax.crypto.Cipher

import scala.util.control.NoStackTrace

object auth {

  @newtype case class UserName(value: String)
  @newtype case class UserId(value: UUID)

  @newtype case class SignUpId(value: String)

  @newtype case class EncryptedSignUpId(value: String)

  @newtype case class EncryptCipher(value: Cipher)
  @newtype case class DecryptCipher(value: Cipher)

  // --------- user registration -----------

  @newtype case class SignUpIdParam(value: NonEmptyString) {
    def toDomain: SignUpId = SignUpId(value.value.toLowerCase)
  }

  @newtype case class UserNameParam(value: NonEmptyString) {
    def toDomain: UserName = UserName(value.value.toLowerCase)
  }

  case class CreateUser(signUpId: SignUpIdParam, signUpType: SignUpType, userName: UserNameParam)

  case class AccountAlreadyCreated(signUpId: SignUpId, name: UserName) extends NoStackTrace
  case class InvalidUserOrPassword(signUpId: SignUpId) extends NoStackTrace
  case object UnsupportedOperation extends NoStackTrace
  case class InvalidFacebookId(signUpId: SignUpId) extends NoStackTrace
  case class InvalidGoogleIdId(signUpId: SignUpId) extends NoStackTrace
  case object TokenNotFound extends NoStackTrace

  // --------- user login -----------

  case class LoginUser(signUpIdParam: SignUpIdParam, signUpType: SignUpType, userName: UserName)

  // --------- admin auth -----------

  @newtype case class ClaimContent(uuid: UUID)

  object ClaimContent {
    implicit val jsonDecoder: Decoder[ClaimContent] =
      Decoder.forProduct1("uuid")(ClaimContent.apply)
  }

}
