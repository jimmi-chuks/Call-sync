package com.dani.contactsynchttp4s

import java.util.UUID

import cats.Eq
import cats.implicits._
import io.estatico.newtype.Coercible
import io.estatico.newtype.ops._

package object domain {

  // does not work as implicit for some reason...
  private def coercibleEq[A: Eq, B: Coercible[A, *]]: Eq[B] =
    (x: B, y: B) => Eq[A].eqv(x.repr.asInstanceOf[A], y.repr.asInstanceOf[A])

  implicit def coercibleStringEq[B: Coercible[String, *]]: Eq[B] =
    coercibleEq[String, B]

  implicit def coercibleUuidEq[B: Coercible[UUID, *]]: Eq[B] =
    coercibleEq[UUID, B]

  implicit def coercibleIntEq[B: Coercible[Int, *]]: Eq[B] =
    coercibleEq[Int, B]

}
