package com.dani.contactsynchttp4s.enums

import enumeratum.EnumEntry.Lowercase
import enumeratum._
import skunk.Codec
import skunk.data.Type
import skunk.codec.`enum`._

sealed trait SignUpType extends EnumEntry with Lowercase

object SignUpType extends Enum[SignUpType] with CirceEnum[SignUpType]{
  case object GoogleAccount extends SignUpType
  case object FacebookAccount extends SignUpType

  val values = findValues

  val signUpTypeCodec: Codec[SignUpType] = enum(SignUpType, Type("signuptype"))
}
