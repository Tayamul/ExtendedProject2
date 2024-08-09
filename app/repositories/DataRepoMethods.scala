package repositories

import models.{APIError, DataModel}
import org.mongodb.scala.result

import scala.concurrent.{ExecutionContext, Future}

trait DataRepoMethods {
  def index(): Future[Either[APIError.BadAPIResponse, Seq[DataModel]]]
  def create(user: DataModel): Future[Either[APIError.BadAPIResponse, DataModel]]
  def read(username: String): Future[Either[APIError.BadAPIResponse, Option[DataModel]]]
  def update(username: String, book: DataModel): Future[Either[APIError.BadAPIResponse, result.UpdateResult]]
  def delete(username: String): Future[Either[APIError.BadAPIResponse, result.DeleteResult]]
}
