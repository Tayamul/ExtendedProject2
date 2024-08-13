package services
import java.util.Base64
import cats.data.EitherT
import connectors.GitHubConnector
import models.{APIError, DataModel, GitHubUser, RepoContentItem, RepoFileItem, Repository}
import play.api.libs.json.OFormat
import play.shaded.ahc.org.asynchttpclient.Response

import javax.inject._
import scala.concurrent.impl.Promise
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GitHubService @Inject()(gitHubConnector: GitHubConnector) {

  def getUserByUserName(urlOverride: Option[String] = None, username: String)(implicit ec: ExecutionContext): EitherT[Future, APIError, GitHubUser] = {
    val url = urlOverride.getOrElse(s"https://api.github.com/users/$username")
    val userOrError = gitHubConnector.get[GitHubUser](url)
    userOrError
  }

  def getUserObjToStore(urlOverride: Option[String] = None, username: String)(implicit ec: ExecutionContext): EitherT[Future, APIError, GitHubUser] = {
    val url = urlOverride.getOrElse(s"https://api.github.com/users/$username")
    val retrieveUserObj = gitHubConnector.get[GitHubUser](url)
    retrieveUserObj
  }


  def getUserRepos(urlOverride: Option[String] = None, username: String)(implicit ec: ExecutionContext): EitherT[Future, APIError, Seq[Repository]] = {
    val url = urlOverride.getOrElse(s"https://api.github.com/users/$username/repos")
    val userRepoResponse = gitHubConnector.get[Seq[Repository]](url)
    userRepoResponse
  }

  def getUserRepoByRepoName(urlOverride: Option[String] = None, username: String, repoName: String)(implicit ec: ExecutionContext): EitherT[Future, APIError, Repository] = {
    val url = urlOverride.getOrElse(s"https://api.github.com/repos/$username/$repoName")
    val userRepoOrError = gitHubConnector.get[Repository](url)
    userRepoOrError
  }

  def getUserRepoContent(urlOverride: Option[String] = None, username: String, repoName: String)(implicit ec: ExecutionContext): EitherT[Future, APIError, Seq[RepoContentItem]] = {
    val url = urlOverride.getOrElse(s"https://api.github.com/repos/$username/$repoName/contents")
    val userRepoContentOrError = gitHubConnector.get[Seq[RepoContentItem]](url)
    userRepoContentOrError
  }


  def getUserRepoDirContent(urlOverride: Option[String] = None, username: String, repoName: String, path:String)(implicit ec: ExecutionContext): EitherT[Future, APIError, Seq[RepoContentItem]] = {
    val url = urlOverride.getOrElse(s"https://api.github.com/repos/$username/$repoName/contents/$path")
    val userRepoContentOrError = gitHubConnector.get[Seq[RepoContentItem]](url)
    userRepoContentOrError
  }

  def getUserRepoFileContent(urlOverride: Option[String] = None, username: String, repoName: String, path:String)(implicit ec: ExecutionContext): EitherT[Future, APIError, RepoFileItem] = {
    val url = urlOverride.getOrElse(s"https://api.github.com/repos/$username/$repoName/contents/$path")
    val userRepoContentOrError = gitHubConnector.get[RepoFileItem](url)
    userRepoContentOrError
  }

  def convertContentToPlainText(content: String): String = {
    try {
      val cleanedContent = content.replaceAll("\\n", "")
      val decodedContent = new String(Base64.getDecoder.decode(cleanedContent), "UTF-8")
      val cleanedContent2 = decodedContent.replaceAll("\\n", "")
      cleanedContent2
    } catch {
      case e: IllegalArgumentException =>
        throw new IllegalArgumentException(s"Failed to decode content: ${e.getMessage}")
    }
  }

  def baseEncodePath(path:String):String = {
    val encodedPath = Base64.getEncoder.encodeToString(path.getBytes("UTF-8"))
    encodedPath
  }
}
