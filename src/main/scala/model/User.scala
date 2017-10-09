package model

import java.util.UUID

import model.CommunicationType._
import service.ChannelCommunicator

case class User(id: UUID, name: String, communicationTypes: Set[CommunicationType]) extends ChannelCommunicator{

  def receiveMessage(message: Message, phoneNumber: Option[PhoneNumber]): Boolean = {
    val communication = communicationTypes.find(_.==(message.messageType))

    (communication, phoneNumber) match {
      case (Some(SMS), Some(phNumber)) =>
        println(s"Hi, I am $name, just got message from: ${phNumber.number}, message text : ${message.text}")
        sendSMS(Message(s"this is response from user $name to channel ${phNumber.number}", SMS), phNumber)
        true
      case (Some(Email), _) =>
        println(s"Hi, I am $name, received an email, Message is ${message.text}")
        true
      case _ => throw new Exception(s"unsupported message type message ${message.messageType}")
    }

  }

}