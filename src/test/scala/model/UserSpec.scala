package model

import java.util.UUID

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import model.CommunicationType._
import service.ChannelSMSHandler

class UserSpec extends Specification with Mockito{

  "user" should {

    "receive SMS message" in {
      val message = Message("test msg", SMS)
      val phoneNumber = PhoneNumber("88888")
      val user = User(UUID.randomUUID(), "Ashok", Set(SMS))

      user.receiveMessage(message, Some(phoneNumber)) must beTrue
    }

    "receive Email message" in {
      val message = Message("test msg", Email)
      val phoneNumber = PhoneNumber("88888")
      val user = User(UUID.randomUUID(), "Amol", Set(Email, SMS))

      user.receiveMessage(message, Some(phoneNumber)) must beTrue
    }



  }



}
