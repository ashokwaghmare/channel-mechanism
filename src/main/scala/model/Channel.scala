package model

import java.util.UUID

case class Channel(id: UUID, name: String, phoneNumber: Option[PhoneNumber]) {

  def receiveMessage(message: Message):Boolean = {
    println(s"Hi, I am $name, got message from follower: ${message.text}")
    true
  }

  def changePhoneNumber(updatedPhoneNumber: PhoneNumber) = Channel(id, name, Some(updatedPhoneNumber))

}
