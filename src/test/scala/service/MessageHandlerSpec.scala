package service

import java.util.UUID

import data.DataStoreApi
import model.CommunicationType._
import model._
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

class MessageHandlerSpec extends Specification with Mockito{


 "Message Handler" should {

  "subscribe user with channel" in {
     val user = User(UUID.randomUUID(), "Ashok", Set(SMS))
     val channel = Channel(UUID.randomUUID(), "ChannelA", None)
     val mockDataStore = mock[DataStoreApi]

     val messageHandler  = new MessageHandler(mockDataStore)

     messageHandler.subscribe(user, channel)
     there was one(mockDataStore).addFollowings(Following(channel.id, user.id))
  }


  "allocate phone number to channel, when no Number associate for channel and broadcast SMS message to all user" in {
    val message = Message("this is test message", SMS)
    val phoneNumber = PhoneNumber("888989")
    val phoneNumber1 = PhoneNumber("777777")
    val phoneNumber2 = PhoneNumber("99999")
    val channel = Channel(UUID.randomUUID(), "ChannelA", None)
    val updatedChannel = Channel(channel.id, "ChannelA", Some(phoneNumber2))
    val mockUser = mock[User]
    val mockDataStore = mock[DataStoreApi]
    mockUser.communicationTypes returns Set(SMS)
    mockUser.id returns UUID.randomUUID()
    mockDataStore.getUsers(channel.id) returns List(mockUser)
    mockDataStore.getChannels(mockUser.id) returns List(channel)
    mockDataStore.getAllPhoneNumbers returns List(phoneNumber, phoneNumber1, phoneNumber2)
    mockDataStore.allocatePhoneNumberToChannel(channel, phoneNumber2) returns updatedChannel
    mockUser.receiveMessage(message, Some(phoneNumber)) returns true

    val messageHandler  = new MessageHandler(mockDataStore)
    messageHandler.broadcastMessage(channel, message)

    there was two(mockDataStore).getUsers(channel.id)
    there was one(mockDataStore).getChannels(mockUser.id)
    there was one(mockDataStore).allocatePhoneNumberToChannel(channel, phoneNumber2)
    there was one(mockUser).receiveMessage(message, Some(phoneNumber2))
  }

  "broadcast SMS message to all user of a channel" in {
     val message = Message("this is test message", SMS)
     val phoneNumber = PhoneNumber("888989")
     val channel = Channel(UUID.randomUUID(), "ChannelA", Some(phoneNumber))
     val mockUser1 = mock[User]
     val mockUser2 = mock[User]
     val mockDataStore = mock[DataStoreApi]
     mockUser1.communicationTypes returns Set(SMS)
     mockUser2.communicationTypes returns Set(SMS)
     mockUser1.id returns UUID.randomUUID()
     mockUser2.id returns UUID.randomUUID()
     mockDataStore.getUsers(channel.id) returns List(mockUser1, mockUser2)
     mockDataStore.getChannels(mockUser1.id) returns List(channel)
     mockDataStore.getChannels(mockUser2.id) returns List(channel)

     val messageHandler  = new MessageHandler(mockDataStore)
     messageHandler.broadcastMessage(channel, message)

     there was one(mockDataStore).getUsers(channel.id)
     there was one(mockUser1).receiveMessage(message, Some(phoneNumber))
     there was one(mockUser2).receiveMessage(message, Some(phoneNumber))
  }


  "not broadcast message when no user associate for a channel" in {
     val message = Message("this is test message", SMS)
     val phoneNumber = PhoneNumber("888989")
     val channel = Channel(UUID.randomUUID(), "ChannelA", Some(phoneNumber))
     val mockUser = mock[User]
     val mockDataStore = mock[DataStoreApi]
     mockUser.communicationTypes returns Set(SMS)
     mockDataStore.getUsers(channel.id) returns List.empty[User]

     val messageHandler  = new MessageHandler(mockDataStore)
     messageHandler.broadcastMessage(channel, message)

     there was one(mockDataStore).getUsers(channel.id)
     there was no(mockUser).receiveMessage(message, Some(phoneNumber))
  }

  "broadcast Email message to all user of channel" in {
     val message = Message("this is test message", Email)
     val channel = Channel(UUID.randomUUID(), "ChannelA", None)
     val mockUser = mock[User]
     val mockDataStore = mock[DataStoreApi]
     mockUser.communicationTypes returns Set(Email)
     mockDataStore.getUsers(channel.id) returns List(mockUser)

     val messageHandler  = new MessageHandler(mockDataStore)
     messageHandler.broadcastMessage(channel, message)

     there was one(mockDataStore).getUsers(channel.id)
     there was one(mockUser).receiveMessage(message, None)
  }

  "return empty list if no 'collision' exist for user and channel" in {
    val mockDataStore = mock[DataStoreApi]
    val messageHandler  = new MessageHandler(mockDataStore)
    val channel = Channel(UUID.randomUUID(), "ChannelA", Some(PhoneNumber("9999")))
    val user = User(UUID.randomUUID(), "Ashok", Set(SMS))
    mockDataStore.getChannels(user.id) returns List(Channel(UUID.randomUUID(), "ChannelB", Some(PhoneNumber("7777"))))

    messageHandler.checkCollision(user, channel) must beEmpty
  }

  "return list of channel if 'collision' exist for user and channel" in {
    val mockDataStore = mock[DataStoreApi]
    val messageHandler  = new MessageHandler(mockDataStore)
    val channel = Channel(UUID.randomUUID(), "ChannelA", Some(PhoneNumber("9999")))

    val channels = List(Channel(UUID.randomUUID(), "ChannelB", Some(PhoneNumber("9999"))),
      Channel(UUID.randomUUID(), "ChannelC", Some(PhoneNumber("9999"))))

    val user = User(UUID.randomUUID(), "Ashok", Set(SMS))
    mockDataStore.getChannels(user.id) returns channels

    messageHandler.checkCollision(user, channel) mustEqual channels
  }

  "return channel of maximum impact on user" in {
    val mockDataStore = mock[DataStoreApi]
    val messageHandler  = new MessageHandler(mockDataStore)

    val channelA = Channel(UUID.randomUUID(), "ChannelA", Some(PhoneNumber("9999")))
    val channelB = Channel(UUID.randomUUID(), "ChannelB", Some(PhoneNumber("9999")))
    val channelC = Channel(UUID.randomUUID(), "ChannelC", Some(PhoneNumber("9999")))

    mockDataStore.getBroadcastStats(channelA.id) returns Some(9)
    mockDataStore.getBroadcastStats(channelB.id) returns Some(1)
    mockDataStore.getBroadcastStats(channelC.id) returns Some(7)


    messageHandler.getMaxImpactedChannel(List(channelA, channelB, channelC)) mustEqual channelA
  }

  "resolve collision when 'collision' exists" in {
    val message = Message("this is test message", SMS)
    val phoneNumber = PhoneNumber("888989")
    val phoneNumber1 = PhoneNumber("999999")
    val phoneNumber2 = PhoneNumber("11111")

    val channelA = Channel(UUID.randomUUID(), "ChannelA", Some(phoneNumber))
    val channelB = Channel(UUID.randomUUID(), "ChannelB", Some(phoneNumber))

    val mockUser1 = mock[User]
    val mockUser2 = mock[User]
    val mockDataStore = mock[DataStoreApi]
    mockUser1.communicationTypes returns Set(SMS)
    mockUser2.communicationTypes returns Set(SMS)
    mockUser1.id returns UUID.randomUUID()
    mockUser2.id returns UUID.randomUUID()
    mockDataStore.getUsers(channelA.id) returns List(mockUser1, mockUser2)
    mockDataStore.getChannels(mockUser1.id) returns List(channelA, channelB)
    mockDataStore.getChannels(mockUser2.id) returns List(channelA, channelB)
    mockDataStore.getBroadcastStats(channelA.id) returns Some(1)
    mockDataStore.getBroadcastStats(channelB.id) returns Some(5)
    mockDataStore.getAllPhoneNumbers returns List(phoneNumber, phoneNumber1, phoneNumber2)
    mockDataStore.allocatePhoneNumberToChannel(channelA, phoneNumber1) returns Channel(channelA.id, channelA.name, Some(phoneNumber1))

    val messageHandler  = new MessageHandler(mockDataStore)
    messageHandler.broadcastMessage(channelA, message)

    there was three(mockDataStore).getUsers(channelA.id)
    there was one(mockUser1).receiveMessage(message, Some(phoneNumber1))
    there was one(mockUser2).receiveMessage(message, Some(phoneNumber1))
    there was two(mockDataStore).updateBroadcastStats(channelA.id, 1)
  }

 }

}
