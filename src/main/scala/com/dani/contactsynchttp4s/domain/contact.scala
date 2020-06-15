package com.dani.contactsynchttp4s.domain

import java.util.UUID

import com.dani.contactsynchttp4s.domain.auth.UserId
import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.MatchesRegex
import eu.timepit.refined.types.string.NonEmptyString
import io.estatico.newtype.macros.newtype

object contact {
  type PhoneNumberType =
    String Refined MatchesRegex["^((00|\\+)33|0)([0-9]{5}|[0-9]{9})$"]

  @newtype case class PhoneNumber(value: String)
  @newtype case class SavedName(value: String)
  @newtype case class ContactId(value: UUID)
  @newtype case class ContactParam(value: NonEmptyString)

  case class Contact(contactId: ContactId, number: PhoneNumber, savedName: SavedName)

//  @newtype case class BrandName(value: String) {
//    def toBrand(brandId: BrandId): Brand =
//      Brand(brandId, this)
//  }
//
//  @newtype case class BrandParam(value: NonEmptyString) {
//    def toDomain: BrandName = BrandName(value.value.toLowerCase.capitalize)
//  }

  // ------ Create Item ---------
  @newtype case class PhoneNumberParam(value: PhoneNumberType)

  @newtype case class SavedNameParam(value: NonEmptyString)
  case class CreateContactParam(phoneNumberParam: PhoneNumberParam, savedNameParam: SavedNameParam) {
    def toDomain(ownerId: UserId): CreateContact =
      CreateContact(
        PhoneNumber(phoneNumberParam.value.value),
        SavedName(savedNameParam.value.value),
        ownerId
      )
  }

  case class CreateContact(phoneNumber: PhoneNumber, savedName: SavedName, ownerId: UserId)

}
