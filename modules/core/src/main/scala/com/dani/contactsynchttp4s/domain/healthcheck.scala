package com.dani.contactsynchttp4s.domain

import io.estatico.newtype.macros._

object healthcheck {
  @newtype case class RedisStatus(value: Boolean)
  @newtype case class PostgresStatus(value: Boolean)

  case class AppStatus(
      redis: RedisStatus,
      postgres: PostgresStatus
  )
}
