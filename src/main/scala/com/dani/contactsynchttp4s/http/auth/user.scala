package com.dani.contactsynchttp4s.http.auth

import com.dani.contactsynchttp4s.domain.auth.{UserId, UserName}
import com.dani.contactsynchttp4s.enums.{CallType, SignUpType}
import dev.profunktor.auth.jwt.JwtSymmetricAuth
import eu.timepit.refined.W
import eu.timepit.refined.api.Refined
import eu.timepit.refined.boolean.{And, Or}
import eu.timepit.refined.char.Digit
import eu.timepit.refined.collection.{Exists, Forall, NonEmpty, Size}
import eu.timepit.refined.generic.Equal
import eu.timepit.refined.numeric.{Interval, Positive}
import eu.timepit.refined.string.MatchesRegex
import io.estatico.newtype.macros.newtype


object user {

  type Rgx = W.`"""(?=[^\\s]+)(?=(\\w+)@([\\w\\.]+))"""`.T
  type EmailPred = MatchesRegex[Rgx]
  type ValidCallType = List[String] Refined Exists[CallType]

//  val s: W.`"abc"`.T = "abc"
////  val enumValues: W.`CallType.values`.T = CallType.values
//
//  type Age = Int Refined Interval.ClosedOpen[W.`7`.T, W.`77`.T]
//  type CGH = List[Int] Refined Size[Equal[W.`5`.T]]
//
//  type GitHash = String Refined And[
//    Size[Interval.Closed[W.`4`.T, W.`40`.T]],
//    Forall[Or[Digit, Interval.Closed[W.`'a'`.T, W.`'f'`.T]]]
//  ]

  @newtype case class AdminJwtAuth(value: JwtSymmetricAuth)
  @newtype case class UserJwtAuth(value: JwtSymmetricAuth)

  @newtype case class CommonUser(value: User)
  @newtype case class AdminUser(value: User)

  case class User(
                   userId: UserId,
                   name: UserName,
                   signUpType: SignUpType
                 )
}
