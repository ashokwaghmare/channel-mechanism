package service

import model.{Message, PhoneNumber}

trait SMSHandlerApi {
  def senSMS(phoneNumber: PhoneNumber, message: Message):Unit
}
