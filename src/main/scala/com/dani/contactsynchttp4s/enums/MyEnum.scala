package com.dani.contactsynchttp4s.enums

import enumeratum.EnumEntry.Lowercase
import enumeratum._

sealed trait MyEnum extends EnumEntry with Lowercase

object MyEnum extends Enum[MyEnum] {

  case object Foo extends MyEnum
  case object Bar extends MyEnum

  val values = findValues // Call ActionService line 69

}

