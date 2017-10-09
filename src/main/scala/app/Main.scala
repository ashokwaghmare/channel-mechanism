package app

import java.util.UUID

import data.DataStore
import model.{Channel, Message, PhoneNumber, User}
import model.CommunicationType._
import service.MessageHandler

object Main extends App{

  val user1 = User(UUID.randomUUID(), "Ashok", Set(SMS))
  val user2 = User(UUID.randomUUID(), "Amol", Set(SMS))

  DataStore.addUser(user1)
  DataStore.addUser(user2)

  val channelA = Channel(UUID.randomUUID(), "channelA", None)
  val channelB = Channel(UUID.randomUUID(), "channelB", None)

  DataStore.addChannel(channelA)
  DataStore.addChannel(channelB)
  //DataStore.addChannel(channelC)


  val messageHandler  = new MessageHandler(DataStore)

  messageHandler.subscribe(user1, channelA)
  messageHandler.subscribe(user2, channelA)
  //messageHandler.subscribe(user1, channelC)
  messageHandler.subscribe(user2, channelB)
  messageHandler.subscribe(user2, channelB)

  messageHandler.broadcastMessage(channelA, Message("Hello from A", SMS))
  //messageHandler.broadcastMessage(channelB, Message("Hello from B", SMS))
  //messageHandler.broadcastMessage(channelC, Message("Hello from C", SMS))

}
