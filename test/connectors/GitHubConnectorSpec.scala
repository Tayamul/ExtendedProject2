//import connectors.GitHubConnector
//import models.{APIError, GitHubUser}
//import org.scalamock.scalatest.MockFactory
//import org.scalatest.concurrent.ScalaFutures
//import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
//import org.scalatest.wordspec.AnyWordSpecLike
//import play.api.libs.json.{Json, OFormat}
//import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
//
//import scala.concurrent.{ExecutionContext, Future}
//
//class GitHubConnectorSpec extends AnyWordSpecLike with ScalaFutures with MockFactory {
//
//  implicit val ec: ExecutionContext = ExecutionContext.global
//  val mockWsClient: WSClient = mock[WSClient]
//  val mockRequest: WSRequest = mock[WSRequest]
//  val mockResponse: WSResponse = mock[WSResponse]
//  val connector = new GitHubConnector(mockWsClient)
//
//  val testUser: GitHubUser = GitHubUser(
//    "testUserName",
//    Some("London"),
//    50,
//    25,
//    "08/08/2024",
//    "www.github.com",
//    Some("testName")
//  )
//
//  implicit val githubUserFormat: OFormat[GitHubUser] = Json.format[GitHubUser]
//
//  "GitHubConnector" should {
//
//    "handle 200 response correctly" in {
//      (mockWsClient.url(_: String)).expects("https://api.github.com/users/username").returning(mockRequest).once()
//      (mockRequest.get _).expects().returning(Future.successful(mockResponse)).once()
//      (mockResponse.status _).expects().returning(200).once()
//      // Ensure the statusText and json are not called if not needed in this test
//      (mockResponse.statusText _).expects().never()
//      (mockResponse.json _).expects().returning(Json.toJson(testUser)).once()
//
//      whenReady(connector.getUserByUserName[GitHubUser]("username").value) { result =>
//        result shouldBe Right(testUser)
//      }
//    }
//
//    "handle non-200 response correctly" in {
//      (mockWsClient.url(_: String)).expects("https://api.github.com/users/username").returning(mockRequest).once()
//      (mockRequest.get _).expects().returning(Future.successful(mockResponse)).once()
//      (mockResponse.status _).expects().returning(404).once()
//      (mockResponse.statusText _).expects().returning("Not Found").once()
//      (mockResponse.json _).expects().never() // Ensure this is not called
//
//      whenReady(connector.getUserByUserName[GitHubUser]("username").value) { result =>
//        result shouldBe Left(APIError.BadAPIResponse(404, "Not Found"))
//      }
//    }
//
//    "handle network error correctly" in {
//      (mockWsClient.url(_: String)).expects("https://api.github.com/users/username").returning(mockRequest).once()
//      (mockRequest.get _).expects().returning(Future.failed(new RuntimeException("Network Error"))).once()
//
//      whenReady(connector.getUserByUserName[GitHubUser]("username").value) { result =>
//        result shouldBe Left(APIError.BadAPIResponse(500, "Could not connect to API."))
//      }
//    }
//  }
//}


package connectors

import baseSpec.BaseSpec
import cats.data.EitherT
import models.{APIError, GitHubUser}
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import play.api.libs.json.{Json, OFormat}
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import scala.concurrent.{ExecutionContext, Future}

class GitHubConnectorSpec extends BaseSpec with ScalaFutures with MockFactory {

  val mockWsClient: WSClient = mock[WSClient]
  val mockRequest: WSRequest = mock[WSRequest]
  val connector: GitHubConnector = new GitHubConnector(mockWsClient)
  implicit val ec: ExecutionContext = ExecutionContext.global

  val testUser: GitHubUser = GitHubUser(
    "testUserName",
    Some("London"),
    50,
    25,
    "08/08/2024",
    "www.github.com",
    Some("testName")
  )

  implicit val githubUserFormat: OFormat[GitHubUser] = Json.format[GitHubUser]

  "getUserByUserName" should {
    val url = "https://api.github.com/users/username"

    "return a Right" when {
      "the API response status is 200" in {
        val mockResponse = mock[WSResponse]
        (mockResponse.status _).expects().returning(200).once()
        (mockResponse.json _).expects().returning(Json.toJson(testUser)).once()

        (mockWsClient.url(_: String)).expects(url).returning(mockRequest).once()
        (mockRequest.get _).expects().returning(Future.successful(mockResponse)).once()

        whenReady(connector.get[GitHubUser](url).value) { result =>
          result shouldBe Right(testUser)
        }
      }
    }

    "return a Left" when {
      "the API response status is not 200" in {
        val mockResponse: WSResponse = mock[WSResponse]
        val error = APIError.BadAPIResponse(404, "Not Found")

        (mockResponse.status _).expects().returning(404).twice()
        (mockResponse.statusText _).expects().returning("Not Found").once()

        (mockWsClient.url(_: String)).expects(url).returning(mockRequest).once()
        (mockRequest.get _).expects().returning(Future.successful(mockResponse)).once()

        whenReady(connector.get[GitHubUser](url).value) { result =>
          result shouldBe Left(error)
        }
      }


      "the API request fails with an exception" in {
        (mockWsClient.url(_: String)).expects(url).returning(mockRequest).once()
        (mockRequest.get _).expects().returning(Future.failed(new RuntimeException("Network Error"))).once()

        whenReady(connector.get[GitHubUser](url).value.failed) { ex =>
          ex shouldBe a [RuntimeException]
          ex.getMessage shouldEqual "Network Error"
        }
      }
    }
  }
}
