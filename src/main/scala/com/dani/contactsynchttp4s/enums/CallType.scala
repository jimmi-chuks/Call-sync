package com.dani.contactsynchttp4s.enums

sealed abstract class CallType(val label: String)

object CallType {
  case object Missed extends CallType("missed")
  case object Incoming extends CallType("incoming")
  case object OutGoingFailed extends CallType("outgoingfailed")
  case object OutGoingSuccess extends CallType("outgoingsuccess")
  case object Unknown extends CallType("unknown")

  val values = List(Missed, Incoming, OutGoingFailed, OutGoingSuccess, Unknown)

  def validate(label: String): Option[CallType] = values.find(_.label == label)

  def fromLabel(label: String): CallType = values.find(_.label == label) match {
    case Some(value) => value
    case None =>CallType.Unknown
  }



}

//import enumeratum._
//import enumeratum.EnumEntry.Lowercase
//import skunk.Codec
//import skunk.codec.`enum`.`enum`
//import skunk.data.Type
//
//
//sealed trait CallType extends EnumEntry with Lowercase
//
//object CallType extends Enum[CallType] {
//  case object Missed extends CallType
//  case object Incoming extends CallType
//  case object OutGoingFailed extends CallType
//  case object OutGoingSuccess extends CallType
//  case object Unknown extends CallType
//
//  val values = findValues
//
//  val callType: Codec[CallType] = enum(CallType, Type("calltype"))
//}

