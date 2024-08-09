package services

import com.mongodb.client.result.UpdateResult
import models.{APIError, DataModel}
import org.mongodb.scala.result
import repositories.DataRepository

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RepositoryService @Inject()(repository: DataRepository){


  def index()(implicit ec: ExecutionContext): Future[Either[APIError.BadAPIResponse, Seq[DataModel]]]  = {
    repository.index().map{
      case Left(error) => Left(error)
      case Right(users) => Right(users)
    }
  }

  def create(newUser: DataModel)(implicit ec: ExecutionContext): Future[Either[APIError.BadAPIResponse, DataModel]] = {
    repository.create(newUser).map{
      case Left(error) => Left(error)
      case Right(item) => Right(item)
    }
  }

  def read(id: String)(implicit ec: ExecutionContext): Future[Either[APIError.BadAPIResponse, DataModel]] = {
    repository.read(id).map{
      case Left(error) => Left(error)
      case Right(Some(item)) => Right(item)
      case Right(None) => Left(APIError.BadAPIResponse(404, s"No Book Found with id: $id"))
    }
  }

  def update(id: String, book: DataModel)(implicit ec: ExecutionContext): Future[Either[APIError.BadAPIResponse, result.UpdateResult]] = {
    repository.update(id, book).map {
      case Left(error) => Left(error)
      case Right(result: UpdateResult) =>
        if(result.wasAcknowledged()) Right(result)
        else Left(APIError.BadAPIResponse(404, s"$result Not Found"))
      case Right(_) => Left(APIError.BadAPIResponse(500, "unexpected error occurred"))
    }
  }

  def delete(id: String)(implicit ec: ExecutionContext): Future[Either[APIError.BadAPIResponse, result.DeleteResult]] = {
    repository.delete(id).map {
      case Left(error) => Left(error)
      case Right(value) => Right(value)
    }
  }
}
