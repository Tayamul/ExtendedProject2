package repositories

import com.google.inject.ImplementedBy
import models.{APIError, DataModel}
import org.mongodb.scala.result

import scala.concurrent.{ExecutionContext, Future}


@ImplementedBy(classOf[DataRepository])
trait DataRepoMethods {
  def index()(implicit ec: ExecutionContext): Future[Either[APIError.BadAPIResponse, Seq[DataModel]]]
  def create(user: DataModel)(implicit ec: ExecutionContext): Future[Either[APIError.BadAPIResponse, DataModel]]
  def read(username: String)(implicit ec: ExecutionContext): Future[Either[APIError.BadAPIResponse, Option[DataModel]]]
  def update(username: String, book: DataModel)(implicit ec: ExecutionContext): Future[Either[APIError.BadAPIResponse, result.UpdateResult]]
  def delete(username: String)(implicit ec: ExecutionContext): Future[Either[APIError.BadAPIResponse, result.DeleteResult]]
}
