package data

import java.util.UUID

import model.{Channel, Following, PhoneNumber, User}

object DataStore extends DataStoreApi{
  import scala.collection.mutable.{ListBuffer => MList}
  private val followings: MList[Following] = MList.empty[Following]
  private val channels: MList[Channel] = MList.empty[Channel]
  private val users: MList[User] = MList.empty[User]
  private val phoneNumbers: MList[PhoneNumber] = MList(PhoneNumber("99999"), PhoneNumber("77777"), PhoneNumber("88888"), PhoneNumber("55555"))
  private val channelToNumberOfMessage: collection.mutable.Map[UUID, Int] = collection.mutable.Map.empty[UUID, Int]

  override def addUser(user: User):Unit = users += user

  override def addChannel(channel: Channel): Unit = channels += channel

  override def removeChannel(channel: Channel): Unit = channels -= channel

  override def addFollowings(following: Following): Unit = followings += following

  override def addPhoneNumber(phoneNumber: PhoneNumber):Unit = phoneNumbers += phoneNumber

  override def getChannels(userId: UUID): List[Channel] = followings.filter(_.userId == userId).flatMap(f =>
    channels.filter(_.id == f.channelId)
  ).toList

  override def getUsers(channelId: UUID): List[User] = followings.filter(_.channelId == channelId).flatMap{ f =>
    users.filter(_.id == f.userId)
  }.toList

  override def getAllUser: List[User] = users.toList

  override def getAllChannels: List[Channel] = channels.toList

  override def getAllFollowings: List[Following] = followings.toList

  override def getAllPhoneNumbers: List[PhoneNumber] = phoneNumbers.toList

  override def getChannels(phoneNumber: PhoneNumber): List[Channel] = channels.filter(_.phoneNumber.getOrElse("-1") == phoneNumber).toList

  override def allocatePhoneNumberToChannel(channel: Channel, phoneNumber: PhoneNumber): Channel = {
        val updatedChannel = channel.changePhoneNumber(phoneNumber)
        updateChannel(channel, updatedChannel)
  }

  override def updateChannel(oldChannel:Channel, newChannel:Channel): Channel = {
    channels.filter(_.id == oldChannel.id).toList match {
      case List(ch) =>
        removeChannel(ch)
        addChannel(newChannel)
        newChannel
      case Nil => throw new Exception(s"Channel ${oldChannel.id} does not exist to update")
    }
  }

  override def updateBroadcastStats(channelId: UUID, numberOfSMS: Int) : Unit = {
    channelToNumberOfMessage.get(channelId) match {
      case Some(count) =>  channelToNumberOfMessage.put(channelId, count + numberOfSMS)
      case None => channelToNumberOfMessage.put(channelId, numberOfSMS)
    }
  }

  override def getAllBroadcastStats(): Map[UUID, Int] = channelToNumberOfMessage.toMap

  override def getBroadcastStats(channelId: UUID): Option[Int] = channelToNumberOfMessage.get(channelId)

}
