package data

import java.util.UUID

import model.{Channel, Following, PhoneNumber, User}

trait DataStoreApi {

  def getChannels(userId: UUID): List[Channel]

  def getChannels(phoneNumber: PhoneNumber): List[Channel]

  def getUsers(channelId: UUID): List[User]

  def getAllUser: List[User]

  def getAllChannels: List[Channel]

  def getAllFollowings: List[Following]

  def getAllPhoneNumbers: List[PhoneNumber]

  def addUser(user: User):Unit

  def addChannel(channel: Channel): Unit

  def addFollowings(following: Following): Unit

  def addPhoneNumber(phoneNumber: PhoneNumber)

  def removeChannel(channel: Channel)

  def allocatePhoneNumberToChannel(channel: Channel, phoneNumber: PhoneNumber): Channel

  def updateChannel(oldChannel: Channel, newChannel: Channel): Channel

  def updateBroadcastStats(channelId: UUID, numberOfSMS: Int) : Unit

  def getAllBroadcastStats : Map[UUID, Int]

  def getBroadcastStats(channelId: UUID): Option[Int]

}
