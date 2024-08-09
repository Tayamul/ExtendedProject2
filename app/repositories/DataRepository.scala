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
    Indexes.ascending("_username")
  )),
  replaceIndexes = false
) with DataRepoMethods {
  def index(): Future[Either[APIError.BadAPIResponse, Seq[DataModel]]]  = {
    collection
      .find()
      .toFuture()
      .map { books =>
        Right(books)
      }.recover {
        case _ =>
          Left(APIError.BadAPIResponse(500, s"An error occurred"))
      }
  }

  def create(user: DataModel): Future[Either[APIError.BadAPIResponse, DataModel]] =
    collection
      .insertOne(user)
      .toFuture().map(_ => Right(user)
      ).recover{
        case ex: Exception => Left(APIError.BadAPIResponse(500, s"An error occurred when trying to add book with id: ${user._username}"))
      }

  private def byUserName(username: String): Bson =
    Filters.and(
      Filters.equal("_username", username)
    )

  def read(username: String): Future[Either[APIError.BadAPIResponse, Option[DataModel]]] =
    collection.find(byUserName(username)).headOption().map { data =>
      Right(data)
    }.recover {
      case ex: Exception => Left(APIError.BadAPIResponse(500, s"An error occurred: ${ex.getMessage}"))
    }

  def update(username: String, book: DataModel): Future[Either[APIError.BadAPIResponse, result.UpdateResult]] =
    collection.replaceOne(
      filter = byUserName(username),
      replacement = book,
      options = new ReplaceOptions().upsert(true) // if upsert set to false, no document created if no match, will throw error
    ).toFuture().map(Right(_)).recover {
      case ex: Exception => Left(APIError.BadAPIResponse(500, s"An error occurred: ${ex.getMessage}"))
    }

  def delete(username: String): Future[Either[APIError.BadAPIResponse, result.DeleteResult]] =
    collection.deleteOne(byUserName(username)).toFuture().map { deleteResult =>
      if (deleteResult.getDeletedCount > 0) Right(deleteResult)
      else Left(APIError.BadAPIResponse(404, s"No item found with id: $username"))
    }.recover {
      case ex: Exception => Left(APIError.BadAPIResponse(500, s"An error occurred: ${ex.getMessage}"))
    }

  def deleteAll(): Future[Unit] = collection.deleteMany(Filters.empty()).toFuture().map(_ => ())
}

