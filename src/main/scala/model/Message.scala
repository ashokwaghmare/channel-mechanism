package model

import model.CommunicationType.CommunicationType

case class Message(text: String, messageType: CommunicationType)