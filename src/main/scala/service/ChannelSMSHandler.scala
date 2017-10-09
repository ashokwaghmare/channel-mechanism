package service

import data.DataStoreApi
import model.PhoneNumber
import model.Message

class ChannelSMSHandler(dataStoreApi: DataStoreApi) extends SMSHandlerApi {

  override def senSMS(phoneNumber : PhoneNumber, message: Message): Unit = {
    dataStoreApi.getChannels(phoneNumber).foreach(_.receiveMessage(message))
  }

}
