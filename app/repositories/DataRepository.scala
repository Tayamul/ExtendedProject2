package repositories

import com.google.inject._
import models.{APIError, DataModel}
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model._
import org.mongodb.scala.result
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DataRepository @Inject()(
                                mongoComponent: MongoComponent
                              )(implicit ec: ExecutionContext) extends PlayMongoRepository[DataModel](
  collectionName = "dataModels",
  mongoComponent = mongoComponent,
  domainFormat = DataModel.formats,
  indexes = Seq(IndexModel(
    Indexes.ascending("_id")
  )),
  replaceIndexes = false
) with DataRepoMethods {

  def index()(implicit ec: ExecutionContext): Future[Either[APIError.BadAPIResponse, Seq[DataModel]]] = {
    collection
      .find()
      .toFuture()
      .map(models => Right(models))
      .recover {
        case _ => Left(APIError.BadAPIResponse(500, "An error occurred"))
      }
  }

  def create(user: DataModel)(implicit ec: ExecutionContext): Future[Either[APIError.BadAPIResponse, DataModel]] = {
    collection.find(byUserName(user._id)).headOption().flatMap {
      case Some(_) =>
        Future.successful(Left(APIError.BadAPIResponse(409, "Username already exists")))
      case None =>
        collection.insertOne(user).toFuture().map(_ => Right(user)).recover {
          case ex: Exception => Left(APIError.BadAPIResponse(500, s"An error occurred when trying to add user with id: ${user._id}"))
        }
    }
  }

  private def byUserName(username: String): Bson =
    Filters.and(Filters.equal("_id", username))

  def read(username: String)(implicit ec: ExecutionContext): Future[Either[APIError.BadAPIResponse, Option[DataModel]]] =
    collection.find(byUserName(username)).headOption().map(data => Right(data)).recover {
      case ex: Exception => Left(APIError.BadAPIResponse(500, s"An error occurred: ${ex.getMessage}"))
    }

  def update(username: String, user: DataModel)(implicit ec: ExecutionContext): Future[Either[APIError.BadAPIResponse, result.UpdateResult]] = {
    collection.replaceOne(
      filter = byUserName(username),
      replacement = user,
      options = new ReplaceOptions().upsert(false) // Change upsert to false
    ).toFuture().map { result =>
      if (result.getModifiedCount > 0) Right(result)
      else Left(APIError.BadAPIResponse(404, s"No item found with id: $username"))
    }.recover {
      case _ => Left(APIError.BadAPIResponse(500, "An error occurred"))
    }
  }


  def delete(username: String)(implicit ec: ExecutionContext): Future[Either[APIError.BadAPIResponse, result.DeleteResult]] =
    collection.deleteOne(byUserName(username)).toFuture().map { deleteResult =>
      if (deleteResult.getDeletedCount > 0) Right(deleteResult)
      else Left(APIError.BadAPIResponse(404, s"No item found with id: $username"))
    }.recover {
      case ex: Exception => Left(APIError.BadAPIResponse(500, s"An error occurred: ${ex.getMessage}"))
    }



  def deleteAll(): Future[Unit] = collection.deleteMany(Filters.empty()).toFuture().map(_ => ())
}
