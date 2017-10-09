package service

import model.{Channel, Message, User}

trait MessageHandlerApi {
  def subscribe(user : User, channel: Channel)
  def broadcastMessage(channel: Channel, message: Message)
}
