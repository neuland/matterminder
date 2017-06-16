package de.neuland.client

import javax.inject.Inject

import akka.actor.{Actor, Props}
import de.neuland.client.WebhookClient.SendRemind
import play.api.Logger
import play.api.libs.ws.WSClient
import play.api.libs.json._

object WebhookClient {
  def props = Props[WebhookClient]
  
  case class SendRemind(text: String, channel: String)
}

class WebhookClient @Inject() (ws: WSClient) extends Actor {
  
  override def receive: Receive = {
    case SendRemind(text: String, channel: String) => {
      val url = "http://localhost:8065/hooks/qexn1bcygj8yxxgxat51rjr51o"

      Logger.debug("Try to send remind to mattermost...")

      val data = Json.obj(
        "text" -> text,
        "channel" -> channel,
        "username" -> "Mattermind Bot"
      )
      ws.url(url).post(data)
    }
  }
  
}
