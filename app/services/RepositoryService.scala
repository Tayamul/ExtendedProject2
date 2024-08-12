package services

import com.mongodb.client.result.UpdateResult
import models.APIError.BadAPIResponse
import models.{APIError, DataModel, GitHubUser}
import org.mongodb.scala.result
import org.mongodb.scala.result.DeleteResult
import repositories.DataRepoMethods

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RepositoryService @Inject()(repository: DataRepoMethods)(implicit ec: ExecutionContext){


  def index()(implicit ec: ExecutionContext): Future[Either[APIError.BadAPIResponse, Seq[DataModel]]]  = {
    repository.index().map{
      case Left(error) => Left(error)
      case Right(users) =>
        if(users.length < 1) Left(APIError.BadAPIResponse(404, "No Users In Database"))
        else Right(users)
    }
  }

  def create(newUser: DataModel)(implicit ec: ExecutionContext): Future[Either[APIError.BadAPIResponse, DataModel]] = {
    repository.create(newUser).map{
      case Left(error) => Left(error)
      case Right(item) => Right(item)
    }
  }

  private def convertDataType(user: GitHubUser): DataModel = {
    DataModel(
      _id = user.login,
      dateCreated = user.created_at,
      location = user.location.getOrElse(""),
      numFollowers = user.followers,
      numFollowing = user.following,
      repoUrl = user.repos_url,
      name = user.name.getOrElse("")
    )
  }


  def read(username: String)(implicit ec: ExecutionContext): Future[Either[APIError.BadAPIResponse, DataModel]] = {
    repository.read(username).map{
      case Left(error) => Left(error)
      case Right(Some(item)) => Right(item)
      case Right(None) => Left(APIError.BadAPIResponse(404, s"No User Found with username: $username"))
    }
  }


  def update(username: String, user: DataModel)(implicit ec: ExecutionContext): Future[Either[APIError.BadAPIResponse, result.UpdateResult]] = {
    repository.update(username, user).map {
      case Left(error) => Left(error)
      case Right(result: UpdateResult) =>
        if (result.wasAcknowledged()) Right(result)
        else Left(APIError.BadAPIResponse(404, s"$result Not Found"))
    }
  }


  def delete(username: String)(implicit ec: ExecutionContext): Future[Either[APIError.BadAPIResponse, result.DeleteResult]] = {
    repository.delete(username).map {
      case Left(error) => Left(error)
      case Right(result: DeleteResult) =>
        if(result.wasAcknowledged()) Right(result)
        else Left(APIError.BadAPIResponse(404, s"$result Not Found"))
    }
  }


}
