package service

import data.{DataStore, DataStoreApi}
import model.CommunicationType._
import model._

class MessageHandler(dataStore: DataStoreApi) extends MessageHandlerApi{


  override def subscribe(user : User, channel: Channel):Unit = {
    dataStore.addFollowings(Following(channel.id, user.id))
  }

  override def  broadcastMessage(channel: Channel, message: Message): Unit = {
    dataStore.getUsers(channel.id).foreach{
      user =>
        val userCommunicationSupport: Option[CommunicationType] = user.communicationTypes.find(_.==(message.messageType))
        val collisions = checkCollision(user, channel)
        val updatedChannel = if(collisions.isEmpty) channel else resolveCollision(collisions, channel, user)

        (userCommunicationSupport,  updatedChannel.phoneNumber) match {
        case (Some(SMS), Some(phoneNumber)) =>
          dataStore.updateBroadcastStats(channel.id, 1)
          user.receiveMessage(message, updatedChannel.phoneNumber)
        case (Some(SMS), None) =>
          val phoneNumber = fetchUniqueNumber(user)
          val updatedChannel = allocatePhoneNumberToChannel(channel, phoneNumber)
          dataStore.updateBroadcastStats(updatedChannel.id, 1)
          user.receiveMessage(message, Some(phoneNumber))
        case (Some(Email), _) => user.receiveMessage(message, None)
        case _ => throw new Exception(s"communication failed for user ${user.name}")
      }

    }
  }

  def checkCollision(user: User, channel: Channel): List[Channel] = {
    if(channel.phoneNumber.isDefined){
      val allChannelsForUser:List[Channel] = dataStore.getChannels(user.id)
      allChannelsForUser.filter(c => (c.phoneNumber == channel.phoneNumber) && c.id != channel.id)
    } else{
      List.empty[Channel]
    }
  }


  private def fetchUniqueNumber(user:User): PhoneNumber = {
    DataStore.getAllPhoneNumbers diff avoidNumbers(user) match {
      case head::list => head
      case Nil => throw new Exception("please add more phone numbers")
    }
  }

  private def allocatePhoneNumberToChannel(channel: Channel, phoneNumber: PhoneNumber): Channel = {
    val updatedChannel = dataStore.allocatePhoneNumberToChannel(channel, phoneNumber)
    dataStore.getUsers(updatedChannel.id).foreach(_.receiveMessage(Message(s"allocated new number to channel: ${channel.name}  $phoneNumber",SMS), Some(phoneNumber)))
    updatedChannel
  }


  private def avoidNumbers(user: User) : List[PhoneNumber] = {
    val allChannelsForUser:List[Channel] = dataStore.getChannels(user.id)
    allChannelsForUser.flatMap(_.phoneNumber)
  }

  private def resolveCollision(channels: List[Channel], ch: Channel, user: User): Channel = {
    val mixChannels = channels :+ ch
    val maxImpactChannel = getMaxImpactedChannel(mixChannels)
    val minimalImpactChannels = mixChannels.filterNot(_ == maxImpactChannel)
    val phoneNumbersToAllocate = getDifferentPhoneNumbers(minimalImpactChannels.size, maxImpactChannel.phoneNumber.get, user)

    val updatedChannels: List[Channel] = (minimalImpactChannels zip phoneNumbersToAllocate).map{
      case (channel : Channel, phoneNumber: PhoneNumber) =>  allocatePhoneNumberToChannel(channel, phoneNumber)
    }

    if(ch == maxImpactChannel) ch else updatedChannels.find(c => c.id == ch.id).get
  }

    def getMaxImpactedChannel(channels: List[Channel]): Channel = {
    channels.flatMap(channel => Map(channel -> dataStore.getBroadcastStats(channel.id))).maxBy(_._2)._1
  }

  private def getDifferentPhoneNumbers(totalRequiredNumbers: Int, exceptNumber: PhoneNumber, user: User): List[PhoneNumber] = {
    val allNums = dataStore.getAllPhoneNumbers
    val availableNumbers = allNums diff avoidNumbers(user)
    availableNumbers.size match {
      case s if s == totalRequiredNumbers => availableNumbers
      case s if s > totalRequiredNumbers => availableNumbers.take(totalRequiredNumbers)
      case s if s < totalRequiredNumbers =>  throw new Exception("please add more phone numbers in data store")
    }
  }


}
