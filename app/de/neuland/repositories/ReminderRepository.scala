package de.neuland.repositories

import com.typesafe.config.{Config, ConfigFactory}
import de.neuland.models.ReminderRecord
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.bson.codecs.configuration.CodecRegistry
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.{Completed, MongoClient, MongoCollection, MongoDatabase}
import play.api.Logger

import scala.concurrent.Future

class ReminderRepository {
  val conf: Config = ConfigFactory.load

  val codecRegistry: CodecRegistry = fromRegistries(fromProviders(classOf[ReminderRecord]), DEFAULT_CODEC_REGISTRY )
  val mongoClient: MongoClient = MongoClient(conf.getString("db.mongo"))
  val database: MongoDatabase = mongoClient.getDatabase(conf.getString("db.database")).withCodecRegistry(codecRegistry)
  val collection: MongoCollection[ReminderRecord] = database.getCollection("reminders")

  def save(reminderRecord: ReminderRecord): Future[Completed] = {
    Logger.debug("Save " + reminderRecord)
    collection.insertOne(reminderRecord).head()
  }

  def all(): Future[Seq[ReminderRecord]] =  {
    collection.find().toFuture()
  }

  def find(author: String): Future[Seq[ReminderRecord]] = {
    collection.find(equal("author", author)).toFuture()
  }

}