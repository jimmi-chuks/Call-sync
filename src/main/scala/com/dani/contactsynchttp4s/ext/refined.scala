package com.dani.contactsynchttp4s.ext

import com.dani.contactsynchttp4s.enums.{CallType, SignUpType}
import eu.timepit.refined.api._
import eu.timepit.refined.auto._
import eu.timepit.refined.collection.Size
import eu.timepit.refined.refineV

object refined {

  implicit def validateSizeN[N <: Int, R](implicit w: ValueOf[N]): Validate.Plain[R, Size[N]] =
    Validate.fromPredicate[R, Size[N]](
      _.toString.size == w.value,
      _ => s"Must have ${w.value} digits",
      Size[N](w.value)
    )

//  implicit def validateSignUpType: Validate.Plain[String, CallType] =
//    Validate.fromPredicate(
//      CallType.validate(_).isEmpty == false,
//      x => s"$x Must be a valid CallType label",
//      CallType.fromLabel _
//    )
//
//  val mk: Either[String, Refined[String, CallType]] = refineV[CallType]("missedr")
//
//

}
