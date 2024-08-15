package services

import baseSpec.BaseSpec
import cats.data.EitherT
import connectors.GitHubConnector
import models.{APIError, GitHubUser, RepoContentItem, RepoFileItem, Repository}
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.libs.json.OFormat
import play.api.Configuration
import scala.concurrent.{ExecutionContext, Future}

class GitHubServiceSpec extends BaseSpec with ScalaFutures with MockFactory with GuiceOneServerPerSuite {

  val mockConnector: GitHubConnector = mock[GitHubConnector]
  implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]

  val testService: GitHubService = new GitHubService(mockConnector)

  val username: String = "testUserName"
  val repoName: String = "testRepoName"
  val filePath: String = "README.md"
  val dirPath: String = "src/main"
  val encodedFilePath = "UkVBRE1FLm1k"
  val encodedDirPath = "c3JjL21haW4="

  // Define sample data
  val gitHubUser: GitHubUser = GitHubUser(
    "testUserName",
    Some("London"),
    50,
    25,
    "08/08/2024",
    "www.github.com",
    Some("testName")
  )

  val repos: Seq[Repository] = Seq(
    Repository("repo1", `private` = false, "https://github.com/user/repo1", Some("A test repo")),
    Repository("repo2", `private` = true, "https://github.com/user/repo2", Some("Another test repo"))
  )

  val repoContent: Seq[RepoContentItem] = Seq(
    RepoContentItem("README.md", "https://github.com/user/repo1/README.md", "file"),
    RepoContentItem("src", "https://github.com/user/repo1/src", "dir")
  )
  val encodedPathRepoContent: Seq[RepoContentItem] = Seq(
    RepoContentItem("README.md", "aHR0cHM6Ly9naXRodWIuY29tL3VzZXIvcmVwbzEvUkVBRE1FLm1k", "file"),
    RepoContentItem("src", "aHR0cHM6Ly9naXRodWIuY29tL3VzZXIvcmVwbzEvc3Jj", "dir")
  )

  val fileContent: RepoFileItem = RepoFileItem("README.md", "file", "base64content", "base64")
  val encodedPathFileContent: RepoFileItem = RepoFileItem("README.md", "ZmlsZQ==", "base64content", "base64")


  // Test for getUserByUserName
  "getUserByUserName" should {
    val url = s"https://api.github.com/users/$username"

    "return a Right" when {

      "GitHub connector .get() returns a user object" in {
        (mockConnector.get[GitHubUser](_: String)(_: OFormat[GitHubUser], _: ExecutionContext))
          .expects(url, *, *)
          .returning(EitherT.rightT(gitHubUser))
          .once()

        whenReady(testService.getUserByUserName(Some(url), username).value) { result =>
          result shouldBe Right(gitHubUser)
        }
      }
    }

    "return a Left" when {

      "GitHub connector .get() returns a 404 Not Found Error" in {
        val apiError: APIError = APIError.BadAPIResponse(404, "Not Found.")

        (mockConnector.get[GitHubUser](_: String)(_: OFormat[GitHubUser], _: ExecutionContext))
          .expects(url, *, *)
          .returning(EitherT.leftT(apiError))
          .once()

        whenReady(testService.getUserByUserName(Some(url), username).value) { result =>
          result shouldBe Left(apiError)
        }
      }

      "GitHub connector .get() returns a 500 Internal Server Error" in {
        val apiError: APIError = APIError.BadAPIResponse(500, "Internal Server Error.")

        (mockConnector.get[GitHubUser](_: String)(_: OFormat[GitHubUser], _: ExecutionContext))
          .expects(url, *, *)
          .returning(EitherT.leftT(apiError))
          .once()

        whenReady(testService.getUserByUserName(Some(url), username).value) { result =>
          result shouldBe Left(apiError)
        }
      }
    }
  }

  // Test for getUserRepos
  "getUserRepos" should {
    val url = s"https://api.github.com/users/$username/repos"

    "return a Right" when {
      "GitHub connector .get() returns a sequence of repositories" in {
        (mockConnector.get[Seq[Repository]](_: String)(_: OFormat[Seq[Repository]], _: ExecutionContext))
          .expects(url, *, *)
          .returning(EitherT.rightT(repos))
          .once()

        whenReady(testService.getUserRepos(Some(url), username).value) { result =>
          result shouldBe Right(repos)
        }
      }
    }

    "return a Left" when {
      "GitHub connector .get() returns a 404 Not Found Error" in {
        val apiError: APIError = APIError.BadAPIResponse(404, "Not Found.")

        (mockConnector.get[Seq[Repository]](_: String)(_: OFormat[Seq[Repository]], _: ExecutionContext))
          .expects(url, *, *)
          .returning(EitherT.leftT(apiError))
          .once()

        whenReady(testService.getUserRepos(Some(url), username).value) { result =>
          result shouldBe Left(apiError)
        }
      }

      "GitHub connector .get() returns a 500 Internal Server Error" in {
        val apiError: APIError = APIError.BadAPIResponse(500, "Internal Server Error.")

        (mockConnector.get[Seq[Repository]](_: String)(_: OFormat[Seq[Repository]], _: ExecutionContext))
          .expects(url, *, *)
          .returning(EitherT.leftT(apiError))
          .once()

        whenReady(testService.getUserRepos(Some(url), username).value) { result =>
          result shouldBe Left(apiError)
        }
      }
    }
  }

  // Test for getUserRepoByRepoName
  "getUserRepoByRepoName" should {
    val url = s"https://api.github.com/repos/$username/$repoName"
    val repo = repos.head // Access the first repository from the defined repos sequence

    "return a Right" when {
      "GitHub connector .get() returns a repository object" in {
        (mockConnector.get[Repository](_: String)(_: OFormat[Repository], _: ExecutionContext))
          .expects(url, *, *)
          .returning(EitherT.rightT(repo))
          .once()

        whenReady(testService.getUserRepoByRepoName(Some(url), username, repoName).value) { result =>
          result shouldBe Right(repo)
        }
      }
    }

    "return a Left" when {
      "GitHub connector .get() returns a 404 Not Found Error" in {
        val apiError: APIError = APIError.BadAPIResponse(404, "Not Found.")

        (mockConnector.get[Repository](_: String)(_: OFormat[Repository], _: ExecutionContext))
          .expects(url, *, *)
          .returning(EitherT.leftT(apiError))
          .once()

        whenReady(testService.getUserRepoByRepoName(Some(url), username, repoName).value) { result =>
          result shouldBe Left(apiError)
        }
      }

      "GitHub connector .get() returns a 500 Internal Server Error" in {
        val apiError: APIError = APIError.BadAPIResponse(500, "Internal Server Error.")

        (mockConnector.get[Repository](_: String)(_: OFormat[Repository], _: ExecutionContext))
          .expects(url, *, *)
          .returning(EitherT.leftT(apiError))
          .once()

        whenReady(testService.getUserRepoByRepoName(Some(url), username, repoName).value) { result =>
          result shouldBe Left(apiError)
        }
      }
    }
  }

  // Test for getUserRepoContent
  "getUserRepoContent" should {
    val url = s"https://api.github.com/repos/$username/$repoName/contents/$filePath"

    "return a Right" when {
      "GitHub connector .get() returns the repository content" in {
        (mockConnector.get[Seq[RepoContentItem]](_: String)(_: OFormat[Seq[RepoContentItem]], _: ExecutionContext))
          .expects(url, *, *)
          .returning(EitherT.rightT(repoContent))
          .once()

        whenReady(testService.getUserRepoContent(Some(url), username, repoName).value) { result =>
          result shouldBe Right(encodedPathRepoContent)
        }
      }
    }

    "return a Left" when {
      "GitHub connector .get() returns a 404 Not Found Error" in {
        val apiError: APIError = APIError.BadAPIResponse(404, "Not Found.")

        (mockConnector.get[Seq[RepoContentItem]](_: String)(_: OFormat[Seq[RepoContentItem]], _: ExecutionContext))
          .expects(url, *, *)
          .returning(EitherT.leftT(apiError))
          .once()

        whenReady(testService.getUserRepoContent(Some(url), username, repoName).value) { result =>
          result shouldBe Left(apiError)
        }
      }

      "GitHub connector .get() returns a 500 Internal Server Error" in {
        val apiError: APIError = APIError.BadAPIResponse(500, "Internal Server Error.")

        (mockConnector.get[Seq[RepoContentItem]](_: String)(_: OFormat[Seq[RepoContentItem]], _: ExecutionContext))
          .expects(url, *, *)
          .returning(EitherT.leftT(apiError))
          .once()

        whenReady(testService.getUserRepoContent(Some(url), username, repoName).value) { result =>
          result shouldBe Left(apiError)
        }
      }
    }
  }

  // Test for getUserRepoDirContent
  "getUserRepoDirContent" should {
    val url = s"https://api.github.com/repos/$username/$repoName/contents/$dirPath"

    "return a Right" when {
      "GitHub connector .get() returns a sequence of repo content items" in {
        (mockConnector.get[Seq[RepoContentItem]](_: String)(_: OFormat[Seq[RepoContentItem]], _: ExecutionContext))
          .expects(url, *, *)
          .returning(EitherT.rightT(repoContent))
          .once()

        whenReady(testService.getUserRepoDirContent(None, username, repoName, encodedDirPath).value) { result =>
          result shouldBe Right(encodedPathRepoContent)
        }
      }
    }

    "return a Left" when {
      "GitHub connector .get() returns a 404 Not Found Error" in {
        val apiError: APIError = APIError.BadAPIResponse(404, "Not Found.")

        (mockConnector.get[Seq[RepoContentItem]](_: String)(_: OFormat[Seq[RepoContentItem]], _: ExecutionContext))
          .expects(url, *, *)
          .returning(EitherT.leftT(apiError))
          .once()

        whenReady(testService.getUserRepoDirContent(None, username, repoName, encodedDirPath).value) { result =>
          result shouldBe Left(apiError)
        }
      }

      "GitHub connector .get() returns a 500 Internal Server Error" in {
        val apiError: APIError = APIError.BadAPIResponse(500, "Internal Server Error.")

        (mockConnector.get[Seq[RepoContentItem]](_: String)(_: OFormat[Seq[RepoContentItem]], _: ExecutionContext))
          .expects(url, *, *)
          .returning(EitherT.leftT(apiError))
          .once()

        whenReady(testService.getUserRepoDirContent(None, username, repoName, encodedDirPath).value) { result =>
          result shouldBe Left(apiError)
        }
      }
    }
  }

  // Test for getUserRepoFileContent
  "getUserRepoFileContent" should {
    val url = s"https://api.github.com/repos/$username/$repoName/contents/$filePath"

    "return a Right" when {
      "GitHub connector .get() returns a repo file item" in {
        (mockConnector.get[RepoFileItem](_: String)(_: OFormat[RepoFileItem], _: ExecutionContext))
          .expects(url, *, *)
          .returning(EitherT.rightT(fileContent))
          .once()

        whenReady(testService.getUserRepoFileContent(None, username, repoName, encodedFilePath).value) { result =>
          result shouldBe Right(encodedPathFileContent)
        }
      }
    }

    "return a Left" when {
      "GitHub connector .get() returns a 404 Not Found Error" in {
        val apiError: APIError = APIError.BadAPIResponse(404, "Not Found.")

        (mockConnector.get[RepoFileItem](_: String)(_: OFormat[RepoFileItem], _: ExecutionContext))
          .expects(url, *, *)
          .returning(EitherT.leftT(apiError))
          .once()

        whenReady(testService.getUserRepoFileContent(None, username, repoName, encodedFilePath).value) { result =>
          result shouldBe Left(apiError)
        }
      }

      "GitHub connector .get() returns a 500 Internal Server Error" in {
        val apiError: APIError = APIError.BadAPIResponse(500, "Internal Server Error.")

        (mockConnector.get[RepoFileItem](_: String)(_: OFormat[RepoFileItem], _: ExecutionContext))
          .expects(url, *, *)
          .returning(EitherT.leftT(apiError))
          .once()

        whenReady(testService.getUserRepoFileContent(None, username, repoName, encodedFilePath).value) { result =>
          result shouldBe Left(apiError)
        }
      }
    }
  }
}
