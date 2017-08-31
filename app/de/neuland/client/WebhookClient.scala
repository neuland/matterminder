package de.neuland.client

import javax.inject.Inject

import akka.actor.{Actor, Props}
import com.typesafe.config.{Config, ConfigFactory}
import de.neuland.client.WebhookClient.SendRemind
import play.api.Logger
import play.api.libs.json._
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext

object WebhookClient {
  def props: Props = Props[WebhookClient]
  
  case class SendRemind(text: String, channel: String)
}

class WebhookClient @Inject() (ws: WSClient) extends Actor {

  
  override def receive: Receive = {
    case SendRemind(text: String, channel: String) =>
      implicit val context: ExecutionContext = play.api.libs.concurrent.Execution.Implicits.defaultContext
      val url = s"$protocol://$server:$port/hooks/$outgoingKey"


      val data = Json.obj(
        "text" -> text,
        "channel" -> channel,
        "username" -> "Mattermind Bot"
      )
      Logger.debug(s"Try to send remind to mattermost: $data")
      ws.url(url).post(data).map {response =>
        Logger.debug(s"Response: status: ${response.status} / statusText: ${response.statusText} / body: ${response.body}")
      }
  }

  private val config: Config = ConfigFactory.load("mattermost.conf")
  val protocol: String = config.getString("protocol")
  val server: String = config.getString("server")
  val port: String = config.getString("port")
  val outgoingKey: String = config.getString("key.outgoing")
  
}
