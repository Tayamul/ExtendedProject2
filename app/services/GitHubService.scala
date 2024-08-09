package services

import cats.data.EitherT
import connectors.GitHubConnector
import models.{APIError, GitHubUser}
import play.api.libs.json.OFormat
import play.shaded.ahc.org.asynchttpclient.Response

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GitHubService @Inject()(gitHubConnector: GitHubConnector) {

  def getUserByUserName(username: String)(implicit ec: ExecutionContext): EitherT[Future, APIError, GitHubUser] = {
    val url = s"https://api.github.com/users/$username"
    val userOrError = gitHubConnector.getUserByUserName[GitHubUser](url)
    userOrError
  }

}
