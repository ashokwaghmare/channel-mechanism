package model

trait SMSHandlerApi {
  def senSMS(phoneNumber: PhoneNumber, message: Message)
}
