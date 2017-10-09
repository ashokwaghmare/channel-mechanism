package service

import data.{DataStore, DataStoreApi}
import model.{Message, PhoneNumber}

trait ChannelCommunicator{
  private val sMSHandlerApi = new ChannelSMSHandler(DataStore)
  def sendSMS(message: Message,phoneNumber : PhoneNumber) = sMSHandlerApi.senSMS(phoneNumber, message)
}
