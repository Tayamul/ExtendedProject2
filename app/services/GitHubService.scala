package services

import java.util.Base64
import cats.data.EitherT
import connectors.GitHubConnector
import models.error._
import models.forms._
import models.mongo._
import models.github._
import models.github.delete.{DeleteFile, DeleteResponse}
import models.github.put._

import javax.inject._
import scala.annotation.tailrec
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

    userRepoContentOrError.map { repoContentItems =>
      repoContentItems.map { repoContentItem =>
        val encodedPath = baseEncodePath(repoContentItem.path)
        repoContentItem.copy(path = encodedPath)
      }
    }
  }


  def getUserRepoDirContent(urlOverride: Option[String] = None, username: String, repoName: String, path: String)(implicit ec: ExecutionContext): EitherT[Future, APIError, Seq[RepoContentItem]] = {
    val decodedPath = convertContentToPlainText(path)
    val url = urlOverride.getOrElse(s"https://api.github.com/repos/$username/$repoName/contents/$decodedPath")
    val userRepoDirContentOrError = gitHubConnector.get[Seq[RepoContentItem]](url)

    userRepoDirContentOrError.map { repoDirContentItems =>
      repoDirContentItems.map { repoDirContentItem =>
        val encodedPath = baseEncodePath(repoDirContentItem.path)
        repoDirContentItem.copy(path = encodedPath)
      }
    }
  }

  def getUserRepoFileContent(urlOverride: Option[String] = None, username: String, repoName: String, path: String)(implicit ec: ExecutionContext): EitherT[Future, APIError, RepoFileItem] = {
    val decodedPath = convertContentToPlainText(path)
    val url = urlOverride.getOrElse(s"https://api.github.com/repos/$username/$repoName/contents/$decodedPath")
    val userRepoContentOrError = gitHubConnector.get[RepoFileItem](url)
    userRepoContentOrError.map { FileItem =>
      val encodedPath = baseEncodePath(FileItem.path)
      val decodedContent = convertContentToPlainText(FileItem.content)
      FileItem.copy(path = encodedPath, content = decodedContent)
    }
  }

  def convertContentToPlainText(content: String): String = {
    try {
      val cleanedContent = content.replaceAll("\\n", "")
      val decodedContent = new String(Base64.getDecoder.decode(cleanedContent), "UTF-8")
      val cleanedNewLines = decodedContent.replaceAll("\\n", " ")
      cleanedNewLines
    } catch {
      case e: IllegalArgumentException =>
        throw new IllegalArgumentException(s"Failed to decode content: ${e.getMessage}")
    }
  }

  def baseEncodePath(path: String): String = {
    val encodedPath = Base64.getEncoder.encodeToString(path.getBytes("UTF-8"))
    encodedPath
  }


  /** ---- Utility methods ---- */
  def getPathSequence(path:String):List[(String, String)]={
    // Returns the (dir Name, path to that dir, encoded path to that dir)
    @tailrec
    def getPathSegments(path:Seq[String], prevPath:String, acc:List[(String, String)]):List[(String, String)] = {
      if (path.length <= 1) acc
      else {
        val pathSegment = path.head
        val pathToSegment = if(prevPath.nonEmpty)s"$prevPath/$pathSegment" else s"$pathSegment"
        val encodedPathSegment = baseEncodePath(pathToSegment)
        getPathSegments(path.tail, pathToSegment, acc:+(path.head, encodedPathSegment))
      }
    }
    val decodedPath = convertContentToPlainText(path)
    val splitPath = decodedPath.split("/")
    getPathSegments(splitPath, "", List())
  }

  /** ---- Put methods for creating / updating ---- */

  def getCommitter(name: Option[String], email: Option[String]): Option[Commiter] = {
    if (name.isDefined && email.isDefined) Some(Commiter(name.get, email.get))
    else None
  }

  def convertCreateFileFormToCreateFile(validFileForm: CreateFileForm): Either[APIError, CreateFile] = {
    try {
      Right(
        CreateFile(
          message = validFileForm.message,
          content = validFileForm.content,
          branch = validFileForm.branch,
          committer = getCommitter(validFileForm.committerName, validFileForm.committerEmail),
          author = getCommitter(validFileForm.authorName, validFileForm.authorEmail)
        )
      )
    } catch {
      case _: Throwable => Left(APIError.BadAPIResponse(500, s"Could not create file ${validFileForm.name} with given inputs"))
    }
  }

  def convertUpdateFileFormToUpdateFile(validFileForm: UpdateFileForm, fileSha: String, decodedPath: String): Either[APIError, UpdateFile] = {
    try {
      Right(
        UpdateFile(
          message = validFileForm.message,
          content = validFileForm.content,
          sha = fileSha,
          committer = getCommitter(validFileForm.committerName, validFileForm.committerEmail),
          author = getCommitter(validFileForm.authorName, validFileForm.authorEmail)
        )
      )
    } catch {
      case _: Throwable => Left(APIError.BadAPIResponse(500, s"Could not update file $decodedPath with given inputs"))
    }
  }

  def createFileRequest(urlOverride: Option[String] = None, owner: String, repoName: String, path: String, file: CreateFile)(implicit ec: ExecutionContext): EitherT[Future, APIError, PutResponse] = {
    val decodedPath = convertContentToPlainText(path)
    val url = urlOverride.getOrElse(s"https://api.github.com/repos/$owner/$repoName/contents/$decodedPath")
    val fileWithEncodedContent = file.copy(content = baseEncodePath(file.content))
    val putFileResponseOrError = gitHubConnector.create[PutResponse](url, fileWithEncodedContent)
    putFileResponseOrError.map { fileResponse =>
      val encodedPath = baseEncodePath(fileResponse.content.path)
      fileResponse.copy(content = fileResponse.content.copy(path = encodedPath))
    }
  }


  def updateFileRequest(urlOverride: Option[String] = None, owner: String, repoName: String, path: String, file: UpdateFile)(implicit ec: ExecutionContext): EitherT[Future, APIError, PutResponse] = {
    val decodedPath = convertContentToPlainText(path)
    val url = urlOverride.getOrElse(s"https://api.github.com/repos/$owner/$repoName/contents/$decodedPath")
    val fileWithEncodedContent = file.copy(content = baseEncodePath(file.content))
    val putFileResponseOrError = gitHubConnector.update[PutResponse](url, fileWithEncodedContent)
    putFileResponseOrError.map { fileResponse =>
      val encodedPath = baseEncodePath(fileResponse.content.path)
      fileResponse.copy(content = fileResponse.content.copy(path = encodedPath))
    }
  }

  def deleteFileRequest(urlOverride: Option[String] = None, owner: String, repoName: String, path: String, file: DeleteFile)(implicit ec: ExecutionContext): EitherT[Future, APIError, DeleteResponse] = {
    val decodedPath = convertContentToPlainText(path)
    val url = urlOverride.getOrElse(s"https://api.github.com/repos/$owner/$repoName/contents/$decodedPath")
    val deleteFileResponseOrError = gitHubConnector.delete[DeleteResponse](url, file)
    deleteFileResponseOrError
  }
}
