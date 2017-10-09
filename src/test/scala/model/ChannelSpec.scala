package model

import java.util.UUID

import org.specs2.mutable.Specification
import model.CommunicationType._

class ChannelSpec extends Specification {

  "Channel" should {
    "receive message" in {
      val channel = Channel(UUID.randomUUID(), "ChannelA", Some(PhoneNumber("99999")))
      channel.receiveMessage(Message("test mes", SMS)) must beTrue
    }
  }

  "receive message" in {
      val channel = Channel(UUID.randomUUID(), "ChannelA", Some(PhoneNumber("99999")))
      channel.changePhoneNumber(PhoneNumber("55555")) mustEqual Channel(channel.id, "ChannelA", Some(PhoneNumber("55555")))
  }

}
