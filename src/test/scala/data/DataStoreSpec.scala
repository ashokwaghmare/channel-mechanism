package data

import java.util.UUID

import model.{Channel, Following, PhoneNumber, User}
import model.CommunicationType._
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

class DataStoreSpec extends Specification with Mockito{
sequential

  "data store" should {

    "add user" in {
      DataStore.addUser(User(UUID.randomUUID(), "Ashok", Set(SMS)))
      DataStore.addUser(User(UUID.randomUUID(), "Swati", Set(SMS, Email)))

      DataStore.getAllUser.size must equalTo(2)
    }

    "add channel" in {
      DataStore.addChannel(Channel(UUID.randomUUID(), "ChannelA", None))
      DataStore.addChannel(Channel(UUID.randomUUID(), "ChannelB", None))
      DataStore.addChannel(Channel(UUID.randomUUID(), "ChannelC", None))

      DataStore.getAllChannels.size must equalTo(3)
    }

    "return empty channel list when no mapping present for userId" in {
      val userId = UUID.randomUUID()
      DataStore.getChannels(userId) must beEmpty
    }

    "return empty user list when no mapping present for channelId" in {
      val channelId = UUID.randomUUID()
      DataStore.getUsers(channelId) must beEmpty
    }

    "add following mapping" in {
      val userId = DataStore.getAllUser.head.id
      val channelID = DataStore.getAllChannels.head.id

      DataStore.addFollowings(Following(channelID, userId))
      DataStore.getAllFollowings.size must equalTo(1)
    }

    "return channel list when user-channel mapping data present" in {
      val userId = DataStore.getAllUser.head.id
      DataStore.getChannels(userId).size must equalTo(1)
      DataStore.getChannels(userId).head.name mustEqual  "ChannelA"
    }

    "return user list when user-channel mapping data present" in {
      val channelID = DataStore.getAllChannels.head.id
      DataStore.getUsers(channelID).size must equalTo(1)
      DataStore.getUsers(channelID).head.name mustEqual "Ashok"
    }

    "be able to add phone number to phone numbers" in {
      DataStore.addPhoneNumber(PhoneNumber("88888"))
      DataStore.getAllPhoneNumbers must contain (PhoneNumber("88888"))
    }

    "be able to fetch channels for given phone Number" in {
      val channel = Channel(UUID.randomUUID(), "channelB", Some(PhoneNumber("77777")))
      DataStore.addChannel(Channel(UUID.randomUUID(), "channelA", Some(PhoneNumber("99999"))))
      DataStore.addChannel(channel)
      DataStore.addChannel(Channel(UUID.randomUUID(), "channelB", None))

      DataStore.getChannels(PhoneNumber("77777")) mustEqual List(channel)
    }

    "update phoneNumber for channel" in {
      val phoneNumber = PhoneNumber("99999")
      val channel = Channel(UUID.randomUUID(), "ChannelA", None)
      DataStore.addChannel(channel)

      DataStore.allocatePhoneNumberToChannel(channel, phoneNumber) mustEqual Channel(channel.id, "ChannelA", Some(phoneNumber))
    }

    "do not allocate phoneNumber if channel does not exist" in {
      val channel = Channel(UUID.randomUUID(), "channelX", None)
      val phoneNumber = PhoneNumber("99999")

      DataStore.allocatePhoneNumberToChannel(channel, phoneNumber) must throwA[Exception]
    }

    "update and fetch broadcast statistics" in {
      val channelA =  Channel(UUID.randomUUID(), "ChannelA", Some(PhoneNumber("99999")))
      val channelB =  Channel(UUID.randomUUID(), "ChannelB", Some(PhoneNumber("99999")))

      DataStore.updateBroadcastStats(channelA.id, 2)
      DataStore.getAllBroadcastStats() mustEqual Map(channelA.id -> 2)
      DataStore.updateBroadcastStats(channelA.id, 5)
      DataStore.getAllBroadcastStats() mustEqual Map(channelA.id -> 7)

      DataStore.updateBroadcastStats(channelB.id, 7)
      DataStore.getBroadcastStats(channelB.id) must beSome(7)

      val randomId = UUID.randomUUID()
      DataStore.getBroadcastStats(randomId) must beNone
    }

  }

}
