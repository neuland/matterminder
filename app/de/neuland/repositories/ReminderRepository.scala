package de.neuland.repositories

import com.typesafe.config.{Config, ConfigFactory}
import de.neuland.models.Reminder
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.bson.codecs.configuration.CodecRegistry
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.{Completed, MongoClient, MongoCollection, MongoDatabase}

import scala.concurrent.Future

class ReminderRepository {
  val conf: Config = ConfigFactory.load

  val codecRegistry: CodecRegistry = fromRegistries(fromProviders(classOf[Reminder]), DEFAULT_CODEC_REGISTRY )
  val mongoClient: MongoClient = MongoClient(conf.getString("db.mongo"))
  val database: MongoDatabase = mongoClient.getDatabase(conf.getString("db.database")).withCodecRegistry(codecRegistry)
  val collection: MongoCollection[Reminder] = database.getCollection("reminders")

  def save(reminder: Reminder): Future[Completed] = {
    collection.insertOne(reminder).head()
  }

  def all(): Future[Seq[Reminder]] =  {
    collection.find().toFuture()
  }

  def find(author: String): Future[Seq[Reminder]] = {
    collection.find(equal("author", author)).toFuture()
  }

}