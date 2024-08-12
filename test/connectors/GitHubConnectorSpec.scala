package connectors

import baseSpec.BaseSpec
import models.GitHubUser
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.libs.json.{Json, OFormat}
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}

import scala.concurrent.{ExecutionContext, Future}

class GitHubConnectorSpec extends BaseSpec with ScalaFutures with MockFactory with GuiceOneServerPerSuite {

  val mockWSClient: WSClient = mock[WSClient]
  val mockRequest: WSRequest = mock[WSRequest]
  val testConnector: GitHubConnector = new GitHubConnector(mockWSClient)
  implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]

  val testUserJSON: String =
    """{
      |"login": "testUserName",
      |"location": "London",
      |"public_repos": 50,
      |"followers": 25,
      |"created_at": "08/08/2024",
      |"blog": "www.github.com",
      |"name": "testName"
      |}""".stripMargin

  val testUser: GitHubUser = GitHubUser(
    "testUserName",
    Some("London"),
    50,
    25,
    "08/08/2024",
    "www.github.com",
    Some("testName")
  )

  implicit val gitHubUserFormat: OFormat[GitHubUser] = Json.format[GitHubUser]

  "getUserByUserName" should {
    val url = "https://api.github.com/users/username"

    "return a Right" when {
      "the API response status is 200" in {
        val mockResponse = mock[WSResponse]
        (mockResponse.status _).expects().returning(200).once()
        (mockResponse.json _).expects().returning(Json.toJson(testUser)).once()

        // Mock the WSRequest to return the mockResponse
        (mockWSClient.url(_: String)).expects(url).returning(mockRequest).once()
        (mockRequest.get _).expects().returning(Future.successful(mockResponse)).once()

        whenReady(testConnector.getUserByUserName[GitHubUser](url).value) { result =>
          result shouldBe Right(testUser)
        }
      }
    }

  }

}
