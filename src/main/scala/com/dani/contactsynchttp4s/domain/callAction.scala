package com.dani.contactsynchttp4s.domain

import java.util.UUID

import com.dani.contactsynchttp4s.domain.auth.UserId
import com.dani.contactsynchttp4s.domain.contact.{ PhoneNumber, PhoneNumberParam }
import com.dani.contactsynchttp4s.enums.CallType
import eu.timepit.refined.types.string.NonEmptyString
import io.estatico.newtype.macros.newtype

object callAction {

  @newtype case class CallId(value: UUID)

  @newtype case class DeviceInfo(value: String)

  @newtype case class CallTime(value: Long)
  @newtype case class CallDuration(value: Long)

  case class CallAction(
      callId: CallId,
      callType: CallType,
      phoneNumber: PhoneNumber,
      callTime: CallTime,
      deviceInfo: DeviceInfo,
      callDuration: CallDuration
  )

  // ------ Create CallAction --------
  @newtype case class CallTypeParam(value: NonEmptyString)

  @newtype case class DeviceInfoParam(value: NonEmptyString)

  @newtype case class CallDurationParam(value: Long)

  case class CreateCallActionParam(
      callTypeParam: CallTypeParam,
      phoneNumberParam: PhoneNumberParam,
      callTime: Long,
      deviceInfoParam: DeviceInfoParam,
      callDurationParam: CallDurationParam,
      callerId: UserId
  ) {

    def toDomain: CreateCallAction = CreateCallAction(
      CallType.fromLabel(callTypeParam.value.value),
      PhoneNumber(phoneNumberParam.value.value),
      CallTime(callTime),
      DeviceInfo(deviceInfoParam.value.value),
      CallDuration(callDurationParam.value),
      callerId
    )

  }

  case class CreateCallAction(
      callType: CallType,
      phoneNumber: PhoneNumber,
      callTime: CallTime,
      deviceInfo: DeviceInfo,
      callDuration: CallDuration,
      callerId: UserId
  )

}
