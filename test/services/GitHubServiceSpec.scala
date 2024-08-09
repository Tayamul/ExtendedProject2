package services

import baseSpec.BaseSpec
import cats.data.EitherT
import connectors.GitHubConnector
import models.{APIError, GitHubUser}
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.libs.json.OFormat

import scala.concurrent.{ExecutionContext, Future}

class GitHubServiceSpec extends BaseSpec with ScalaFutures with MockFactory with GuiceOneServerPerSuite {

  val mockConnector: GitHubConnector = mock[GitHubConnector]
  implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]
  val testService: GitHubService = new GitHubService(mockConnector)

  val username: GitHubUser = GitHubUser(
    "testUserName",
    Some("London"),
    50,
    25,
    "08/08/2024",
    "www.github.com",
    Some("testName")
  )

  "getUserByUserName" should {
    val url = "username"

    "return a Right" when {

      "retrieve the user object for a particular user" in {

        (mockConnector.getUserByUserName[GitHubUser](_: String)(_: OFormat[GitHubUser], _: ExecutionContext))
          .expects(url, *, *)
          .returning(EitherT.rightT(username))
          .once()

        whenReady(testService.getUserByUserName(Some(url), "username").value) { result =>
          result shouldBe Right(username)
        }
      }

    }

    "return a Left" when {

      "Github connector .get() return an error" in {

        val apiError: APIError = APIError.BadAPIResponse(404, "Not Found.")

        (mockConnector.getUserByUserName[GitHubUser](_: String)(_: OFormat[GitHubUser], _: ExecutionContext))
          .expects(url, *, *)
          .returning(EitherT.leftT(apiError))
          .once()

        whenReady(testService.getUserByUserName(Some(url), "username").value) { result =>
          result shouldBe Left(apiError)
        }
      }

    }


  }
}
