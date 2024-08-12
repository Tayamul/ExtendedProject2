package services

import cats.data.EitherT
import connectors.GitHubConnector
import models.{APIError, DataModel, GitHubUser}
import play.api.libs.json.OFormat
import play.shaded.ahc.org.asynchttpclient.Response

import javax.inject._
import scala.concurrent.impl.Promise
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GitHubService @Inject()(gitHubConnector: GitHubConnector) {

  def getUserByUserName(urlOverride: Option[String] = None, username: String)(implicit ec: ExecutionContext): EitherT[Future, APIError, GitHubUser] = {
    val url = urlOverride.getOrElse(s"https://api.github.com/users/$username")
    val userOrError = gitHubConnector.getUserByUserName[GitHubUser](url)
    userOrError
  }

  def convertDataType(user: GitHubUser): DataModel = {
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

  def getUserObjToStore(urlOverride: Option[String] = None, username: String)(implicit ec: ExecutionContext): EitherT[Future, APIError, GitHubUser] = {
    val url = urlOverride.getOrElse(s"https://api.github.com/users/$username")
    val retrieveUserObj = gitHubConnector.getUserObjToStore[GitHubUser](url)
    retrieveUserObj
  }

}
