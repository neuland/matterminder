package modules

import com.google.inject.AbstractModule
import de.neuland.client.WebhookClient
import de.neuland.scheduling.Scheduler
import play.api.libs.concurrent.AkkaGuiceSupport

class MatterminderModule extends AbstractModule with AkkaGuiceSupport {
  
  def configure() = {
    
    bindActor[Scheduler]("scheduler")
    bindActor[WebhookClient]("webhookClient")
    
  }
}