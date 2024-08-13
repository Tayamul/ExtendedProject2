package controllers

import baseSpec.BaseSpecWithApplication
import cats.data.EitherT
import connectors.GitHubConnector
import models.{APIError, DataModel, GitHubUser}
import org.scalamock.scalatest.MockFactory
import play.api.http.Status
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json, OFormat}
import play.api.mvc.{AnyContent, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, contentAsJson, defaultAwaitTimeout, status}
import repositories.DataRepoMethods
import services.{GitHubService, RepositoryService}

import scala.concurrent.{ExecutionContext, Future}

class ApplicationControllerSpec extends BaseSpecWithApplication with MockFactory {


  val mockConnector: GitHubConnector = mock[GitHubConnector]
  val testGitService: GitHubService = new GitHubService(mockConnector)

  val mockDataRepo: DataRepoMethods = mock[DataRepoMethods]
  val testRepoService = new RepositoryService(mockDataRepo)

  val TestController = new ApplicationController(
    component,
    repoService,
    gitService
  )(executionContext)
  val TestControllerMockGitService = new ApplicationController(
    component,
    repoService,
    testGitService
  )(executionContext)
  val TestControllerMockServices = new ApplicationController(
    component,
    testRepoService,
    testGitService
  )(executionContext)

  val testUserDataModel: DataModel = DataModel(
    _id = "testUserName",
    dateCreated = "2020-10-16T09:59:16Z",
    location = "London",
    numFollowers = 1,
    numFollowing = 2,
    repoUrl = "https://api.github.com/users/tbg2003/repos",
    name = "Test User"
  )
  val testUserDataModelDupe: DataModel = DataModel(
    _id = "testUserName",
    dateCreated = "2020-10-16T09:59:16Z",
    location = "London",
    numFollowers = 1,
    numFollowing = 2,
    repoUrl = "https://api.github.com/users/tbg2003/repos",
    name = "Test User"
  )
  val updateUserDataModel: DataModel = DataModel(
    _id = "testUserName",
    dateCreated = "2020-10-16T09:59:16Z",
    location = "London",
    numFollowers = 1,
    numFollowing = 10,
    repoUrl = "https://api.github.com/users/tbg2003/repos",
    name = "Test User"
  )

  val testGitHubUser: GitHubUser = GitHubUser(
    "testUserName",
    Some("London"),
    50,
    25,
    "08/08/2024",
    "www.github.com",
    Some("testName")
  )

  "ApplicationController .index" should {
    "return 200 Ok with body" when {
      "Items in database" in {
        beforeEach()

        val createUserRequest: FakeRequest[JsValue] = testRequest.buildPost("api/user").withBody[JsValue](Json.toJson(testUserDataModel))
        val createdResult = TestController.create()(createUserRequest)
        status(createdResult) shouldBe CREATED

        val indexResult = TestController.index()(FakeRequest())
        status(indexResult) shouldBe OK

        afterEach()
      }
    }

    "return 404 Not Found" when {
      "No items in database" in {
        beforeEach()

        val indexResult = TestController.index()(FakeRequest())
        status(indexResult) shouldBe NOT_FOUND

        afterEach()
      }
    }
  }

  "ApplicationController .create" should {
    "return 201 Created with body" in {
      beforeEach()
      val request: FakeRequest[JsValue] = testRequest.buildPost("/api/user").withBody[JsValue](Json.toJson(testUserDataModel))
      val createdResult: Future[Result] = TestController.create()(request)

      status(createdResult) shouldBe Status.CREATED
      contentAsJson(createdResult).as[DataModel] shouldBe testUserDataModel
      afterEach()
    }

    "return 409 when duplicate username" in {
      beforeEach()

      val request: FakeRequest[JsValue] = testRequest.buildPost("/api/user").withBody[JsValue](Json.toJson(testUserDataModel))
      val request2: FakeRequest[JsValue] = testRequest.buildPost("/api/user").withBody[JsValue](Json.toJson(testUserDataModelDupe))

      val createdResult: Future[Result] = TestController.create()(request)
      status(createdResult) shouldBe CREATED

      val createdResult2: Future[Result] = TestController.create()(request2)
      status(createdResult2) shouldBe CONFLICT

      afterEach()
    }

    "return 400 Bad Request" in {
      beforeEach()
      val badRequestBody: JsValue = Json.parse("""{"username": "abcd", "name": 12345}""")
      val badRequest: FakeRequest[JsValue] = testRequest.buildPost("/api/user").withBody[JsValue](Json.toJson(badRequestBody))
      val createdResult: Future[Result] = TestController.create()(badRequest)

      status(createdResult) shouldBe Status.BAD_REQUEST
      afterEach()
    }
  }

  "ApplicationController .read" should {
    "return 200 Ok with body" in {
      beforeEach()

      val request: FakeRequest[JsValue] = testRequest.buildPost("/api/user").withBody[JsValue](Json.toJson(testUserDataModel))
      val createdResult: Future[Result] = TestController.create()(request)

      status(createdResult) shouldBe Status.CREATED

      val readResult: Future[Result] = TestController.read("testUserName")(FakeRequest())

      status(readResult) shouldBe Status.OK
      contentAsJson(readResult).as[DataModel] shouldBe testUserDataModel

      afterEach()
    }

    "return 404 Not Found with body" in {
      beforeEach()

      val request: FakeRequest[JsValue] = testRequest.buildPost("/api/user").withBody[JsValue](Json.toJson(testUserDataModel))
      val createdResult: Future[Result] = TestController.create()(request)
      status(createdResult) shouldBe Status.CREATED

      val newReadResult = TestController.read("nonExistingUserName")(FakeRequest())

      status(newReadResult) shouldBe Status.NOT_FOUND
      contentAsJson(newReadResult).as[String] shouldBe "No User Found with username: nonExistingUserName"

      afterEach()
    }
  }

  "ApplicationController .update" should {
    "return 202 accepted with body" in {
      beforeEach()

      val createRequest: FakeRequest[JsValue] = testRequest.buildPost("/api/user").withBody[JsValue](Json.toJson(testUserDataModel))
      val createdResult: Future[Result] = TestController.create()(createRequest)
      status(createdResult) shouldBe Status.CREATED

      val updateRequest: FakeRequest[JsValue] = testRequest.buildPut(s"/api/user/${testUserDataModel._id}").withBody[JsValue](Json.toJson(updateUserDataModel))
      val updateResult: Future[Result] = TestController.update("testUserName")(updateRequest)

      status(updateResult) shouldBe Status.ACCEPTED
      contentAsJson(updateResult).as[DataModel] shouldBe updateUserDataModel

      afterEach()
    }

    "return 404 Internal Server Error" in {
      beforeEach()

      val apiError: APIError = APIError.BadAPIResponse(404, s"No item found with id: nonExistingUserName")
      val createRequest: FakeRequest[JsValue] = testRequest.buildPost("/api/user").withBody[JsValue](Json.toJson(testUserDataModel))
      val createdResult: Future[Result] = TestController.create()(createRequest)
      //      status(createdResult) shouldBe Status.CREATED

      val updateRequest: FakeRequest[JsValue] = testRequest.buildPut(s"/api/user/${testUserDataModel._id}").withBody[JsValue](Json.toJson(updateUserDataModel))
      val updateResult: Future[Result] = TestController.update("nonExistingUserName")(updateRequest)

      status(updateResult) shouldBe NOT_FOUND

      afterEach()
    }

    "return 400 Bad Request" in {
      beforeEach()

      val request: FakeRequest[JsValue] = testRequest.buildPost("/api/update/${testUserDataModel._id}").withBody[JsValue](Json.toJson(testUserDataModel))
      val createdResult: Future[Result] = TestController.create()(request)
      status(createdResult) shouldBe Status.CREATED


      val badRequestBody: JsValue = Json.parse("""{"id": "testUserName", "name": 12345}""")
      val badUpdateRequest: FakeRequest[JsValue] = testRequest.buildPut("/api/user").withBody[JsValue](Json.toJson(badRequestBody))
      val updateResult: Future[Result] = TestController.update("testUserName")(badUpdateRequest)

      status(updateResult) shouldBe Status.BAD_REQUEST

      afterEach()
    }
  }

  "ApplicationController .delete" should {
    "return 202 Accepted" in {
      beforeEach()
      val request = testRequest.buildPost("/api/user").withBody[JsValue](Json.toJson(testUserDataModel))
      val createdResult: Future[Result] = TestController.create()(request)
      status(createdResult) shouldBe Status.CREATED

      val deleteResult = TestController.delete(s"${testUserDataModel._id}")(FakeRequest())

      status(deleteResult) shouldBe ACCEPTED
      afterEach()
    }
    "return 404 Not Found Error" in {
      beforeEach()
      val deleteResult = TestController.delete("abc")(FakeRequest())
      status(deleteResult) shouldBe NOT_FOUND
      afterEach()
    }
  }

  "ApplicationController .getGitHubUser" should {
    "return 200 OK with body" when {
      "GitHubService .getUserByUserName returns a user object" in {

        val testUrl = "https://api.github.com/users/testUserName"
        
        (mockConnector.get(_: String)(_: OFormat[GitHubUser], _: ExecutionContext))
          .expects(testUrl, *, *)
          .returning(EitherT.rightT(testGitHubUser))
          .once()

        val getGitHubUserResult = TestControllerMockGitService.getGitHubUser("testUserName")(FakeRequest())
        status(getGitHubUserResult) shouldBe OK
      }
    }
    "returns 404 Not Found API Error" when {
      "GitHubService .getUserByUserName returns an error" in {

        val testUrl = "https://api.github.com/users/testUserName"
        val apiError = APIError.BadAPIResponse(404, "Not Found")

        (mockConnector.get(_: String)(_: OFormat[GitHubUser], _: ExecutionContext))
          .expects(testUrl, *, *)
          .returning(EitherT.leftT(apiError))
          .once()

        val getGitHubUserResult = TestControllerMockGitService.getGitHubUser("testUserName")(FakeRequest())
        status(getGitHubUserResult) shouldBe NOT_FOUND
      }
    }
  }

  "ApplicationController .getUserObj" should {
    "return 200 OK response" when {
      "successfully gets a user from github and stores in database" in {
        val testUserName = s"${testUserDataModel._id}"
        val testUrl = s"https://api.github.com/users/$testUserName"

        (mockConnector.get(_: String)(_: OFormat[GitHubUser], _: ExecutionContext))
          .expects(testUrl, *, *)
          .returning(EitherT.rightT(testGitHubUser))
          .once()


        (mockDataRepo.create(_:DataModel)(_:ExecutionContext))
          .expects(*, *)
          .returning(Future.successful(Right(testUserDataModel)))
          .once()

        val getUserObjResult = TestControllerMockServices.getUserObj(testUserName)(FakeRequest())
        status(getUserObjResult) shouldBe OK
      }
    }
    "return 409 Conflict error" when {
      "User already exists in database" in {
        true
      }
    }
    "return 500 Internal server error" when {
      "internal server error from GitHub API" in {
        true
      }
      "internal server error from MongoDB" in {
        true
      }
    }
  }

  "ApplicationController .getUserRepos"
  "ApplicationController .getUserRepoByRepoName"
  "ApplicationController .getUserRepoContent"
  "ApplicationController .getUserRepoDirContent"
  "ApplicationController .getUserRepoFileContent"

  override def beforeEach(): Unit = await(repository.deleteAll())

  override def afterEach(): Unit = await(repository.deleteAll())
}
