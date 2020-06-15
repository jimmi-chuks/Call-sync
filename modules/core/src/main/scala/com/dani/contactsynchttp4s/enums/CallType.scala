package com.dani.contactsynchttp4s.enums

import enumeratum.EnumEntry.Lowercase
import enumeratum._
import skunk.Codec
import skunk.codec.`enum`._
import skunk.data.Type

sealed trait CallType extends EnumEntry with Lowercase

object CallType extends Enum[CallType] with CirceEnum[CallType] {
  case object Missed extends CallType
  case object Incoming extends CallType
  case object OutGoingFailed extends CallType
  case object OutGoingSuccess extends CallType
  case object Unknown extends CallType

  val values = findValues

  val callTypeCodec: Codec[CallType] = enum(CallType, Type("calltype"))
}
