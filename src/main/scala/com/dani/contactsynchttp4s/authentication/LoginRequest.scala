package com.dani.contactsynchttp4s.authentication


case class LoginRequest(
  signUpId: String,
  signUpType: String
)

case class SignUpRequest(
  userName: String,
  firstName: String,
  lastName: String,
  email: String,
  password: String,
  signUpType: String
)
