import javax.inject.Inject

import de.neuland.services.ReminderService
import play.api.{Application, GlobalSettings}

class Global @Inject() (reminderService: ReminderService) extends GlobalSettings  {

  override def onStart(app: Application): Unit = {
    reminderService.scheduleExistingReminders()
    super.onStart(app)
  }

}