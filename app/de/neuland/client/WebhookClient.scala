package de.neuland.client

import akka.actor.{Actor, Props}
import de.neuland.client.WebhookClient.SendRemind
import play.api.libs.json._
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logging}

import javax.inject.Inject
import scala.concurrent.ExecutionContext

object WebhookClient {
  def props: Props = Props[WebhookClient]()
  
  case class SendRemind(text: String, channel: String, webhookKey: String)
}

class WebhookClient @Inject() (ws: WSClient, config: Configuration) extends Actor with Logging {

  override def receive: Receive = {
    case SendRemind(text: String, channel: String, webhookKey: String) =>
      implicit val context: ExecutionContext = ExecutionContext.global
      val url = s"$protocol://$server:$port/hooks/$webhookKey"

      val data = Json.obj(
        "text" -> text,
        "channel" -> channel,
        "username" -> "Mattermind Bot"
      )
      logger.debug(s"Try to send remind to mattermost: $data")
      ws.url(url).post(data).map {response =>
        logger.debug(s"Response: status: ${response.status} / statusText: ${response.statusText} / body: ${response.body}")
      }
  }

  val protocol: String = config.get[String]("mattermost.protocol")
  val server: String = config.get[String]("mattermost.server")
  val port: String = config.get[String]("mattermost.port")
  
}
