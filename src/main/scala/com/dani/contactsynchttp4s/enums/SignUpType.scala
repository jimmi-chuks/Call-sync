package com.dani.contactsynchttp4s.enums

import enumeratum.EnumEntry.Lowercase
import enumeratum._
import skunk.codec.`enum`.`enum`
import skunk.data.Type

sealed trait SignUpType extends EnumEntry with Lowercase

object SignUpType extends Enum[SignUpType]{
  case object GoogleAccount extends SignUpType
  case object FacebookAccount extends SignUpType

  val values = findValues

  val myenum = enum(SignUpType, Type("signuptype"))
}
