package controllers

import baseSpec.BaseSpecWithApplication
import cats.data.{EitherT, NonEmptyMap}
import connectors.GitHubConnector
import models.error._
import models.forms._
import models.mongo._
import models.github._
import org.scalamock.scalatest.MockFactory
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.http.Status
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json, OFormat, Reads}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents, ControllerHelpers, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, contentAsJson, contentAsString, defaultAwaitTimeout, status, stubControllerComponents}
import repositories.DataRepoMethods
import services.{GitHubService, RepositoryService}
import play.api.Configuration
import play.api.test.CSRFTokenHelper.CSRFRequest

import scala.concurrent.{ExecutionContext, Future}

class ApplicationControllerSpec extends BaseSpecWithApplication with MockFactory {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global


  val mockConnector: GitHubConnector = mock[GitHubConnector]
  val testGitService: GitHubService = new GitHubService(mockConnector)

  val mockDataRepo: DataRepoMethods = mock[DataRepoMethods]
  val testRepoService = new RepositoryService(mockDataRepo)

  val controllerComponents: ControllerComponents = stubControllerComponents()

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


        (mockDataRepo.create(_: DataModel)(_: ExecutionContext))
          .expects(*, *)
          .returning(Future.successful(Right(testUserDataModel)))
          .once()

        val getUserObjResult = TestControllerMockServices.getUserObj(testUserName)(FakeRequest())
        status(getUserObjResult) shouldBe OK
      }
    }
    "return 409 Conflict error" when {
      "User already exists in database" in {
        val testUserName = s"${testUserDataModel._id}"
        val testUrl = s"https://api.github.com/users/$testUserName"

        (mockConnector.get(_: String)(_: OFormat[GitHubUser], _: ExecutionContext))
          .expects(testUrl, *, *)
          .returning(EitherT.rightT(testGitHubUser))
          .once()

        val duplicateUserError = APIError.BadAPIResponse(409, "Username already exists")
        (mockDataRepo.create(_: DataModel)(_: ExecutionContext))
          .expects(*, *)
          .returning(Future(Left(duplicateUserError)))
          .once()

        val getUserObjResult = TestControllerMockServices.getUserObj(testUserName)(FakeRequest())
        status(getUserObjResult) shouldBe CONFLICT

      }
    }
    "return 500 Internal server error" when {
      "internal server error from GitHub API" in {
        val apiError = APIError.BadAPIResponse(500, "Could not connect to API.")

        val testUserName = s"${testUserDataModel._id}"
        val testUrl = s"https://api.github.com/users/$testUserName"

        (mockConnector.get(_: String)(_: OFormat[GitHubUser], _: ExecutionContext))
          .expects(testUrl, *, *)
          .returning(EitherT.leftT(apiError))
          .once()

        val getUserObjResult = TestControllerMockServices.getUserObj(testUserName)(FakeRequest())
        status(getUserObjResult) shouldBe INTERNAL_SERVER_ERROR

      }
      "internal server error from MongoDB" in {
        val apiError = APIError.BadAPIResponse(500, s"An error occurred when trying to add user with id: ${testUserDataModel._id}")
        val testUserName = s"${testUserDataModel._id}"
        val testUrl = s"https://api.github.com/users/$testUserName"

        (mockConnector.get(_: String)(_: OFormat[GitHubUser], _: ExecutionContext))
          .expects(testUrl, *, *)
          .returning(EitherT.rightT(testGitHubUser))
          .once()

        (mockDataRepo.create(_: DataModel)(_: ExecutionContext))
          .expects(*, *)
          .returning(Future(Left(apiError)))
          .once()

        val getUserObjResult = TestControllerMockServices.getUserObj(testUserName)(FakeRequest())
        status(getUserObjResult) shouldBe INTERNAL_SERVER_ERROR


      }
    }
  }

  "ApplicationController .getUserRepos" should {
    "return 200 Ok and a list of repositories for given user" when {
      "user exists and no errors encountered" in {

        val testUserName = s"${testUserDataModel._id}"
        val testUrl = s"https://api.github.com/users/$testUserName/repos"
        val testUserRepos = Seq(
          Repository(name = "Test Name", `private` = false, html_url = "testURL", description = Some("Test Description"))
        )

        (mockConnector.get(_: String)(_: Reads[Seq[Repository]], _: ExecutionContext))
          .expects(testUrl, *, *)
          .returning(EitherT.rightT(testUserRepos))
          .once()

        val getReposResult = TestControllerMockServices.getUserRepos(testUserName)(FakeRequest())
        status(getReposResult) shouldBe OK
      }
    }
    "return 404 Not Found error" when {
      "user doesn't exists" in {
        val testUserName = s"${testUserDataModel._id}"
        val testUrl = s"https://api.github.com/users/$testUserName/repos"
        val apiError = APIError.BadAPIResponse(404, "Not Found")

        (mockConnector.get(_: String)(_: Reads[Seq[Repository]], _: ExecutionContext))
          .expects(testUrl, *, *)
          .returning(EitherT.leftT(apiError))
          .once()

        val getReposResult = TestControllerMockServices.getUserRepos(testUserName)(FakeRequest())
        status(getReposResult) shouldBe NOT_FOUND
      }
    }
    "return 500 internal server error" when {
      "internal server error encountered with GitHub API" in {
        val apiError = APIError.BadAPIResponse(500, "Could not connect to API.")

        val testUserName = s"${testUserDataModel._id}"
        val testWrongUrl = s"https://api.github.com/users/$testUserName"

        (mockConnector.get(_: String)(_: Reads[Seq[Repository]], _: ExecutionContext))
          .expects(testWrongUrl, *, *)
          .returning(EitherT.leftT(apiError))
          .once()

        val getUserObjResult = TestControllerMockServices.getUserObj(testUserName)(FakeRequest())
        status(getUserObjResult) shouldBe INTERNAL_SERVER_ERROR
      }
    }
  }

  "ApplicationController .getUserRepoContent" should {
    "return OK response" when {
      "the user and repo exist" in {
        val testUserName = s"${testUserDataModel._id}"
        val testRepoName = "TestRepoName"
        val testUrl = s"https://api.github.com/repos/$testUserName/$testRepoName/contents"
        val testRepoContent = Seq(RepoContentItem("Test File Name", "testFilePath", "testSha", "file"), RepoContentItem("Test Dir Name", "testDirPath","testSha", "dir"))
        (mockConnector.get(_: String)(_: Reads[Seq[RepoContentItem]], _: ExecutionContext))
          .expects(testUrl, *, *)
          .returning(EitherT.rightT(testRepoContent))
          .once()

        val getUserObjResult = TestControllerMockServices.getUserRepoContent(testUserName, testRepoName)(FakeRequest())
        status(getUserObjResult) shouldBe OK
      }
    }
    "return 404 Not Found response" when {
      "Repository doesn't exist" in {
        val apiError = APIError.BadAPIResponse(404, "Not Found")
        val testBadUserName = s"Invalid Username"
        val testRepoName = "TestRepoName"
        val testUrl = s"https://api.github.com/repos/$testBadUserName/$testRepoName/contents"
        (mockConnector.get(_: String)(_: Reads[Seq[RepoContentItem]], _: ExecutionContext))
          .expects(testUrl, *, *)
          .returning(EitherT.leftT(apiError))
          .once()

        val getUserObjResult = TestControllerMockServices.getUserRepoContent(testBadUserName, testRepoName)(FakeRequest())
        status(getUserObjResult) shouldBe NOT_FOUND
      }
      "User doesn't exist" in {
        val apiError = APIError.BadAPIResponse(404, "Not Found")
        val testUserName = s"${testUserDataModel._id}"
        val testBadRepoName = "Invalid Repo Name"
        val testUrl = s"https://api.github.com/repos/$testUserName/$testBadRepoName/contents"
        (mockConnector.get(_: String)(_: Reads[Seq[RepoContentItem]], _: ExecutionContext))
          .expects(testUrl, *, *)
          .returning(EitherT.leftT(apiError))
          .once()

        val getUserObjResult = TestControllerMockServices.getUserRepoContent(testUserName, testBadRepoName)(FakeRequest())
        status(getUserObjResult) shouldBe NOT_FOUND
      }
    }
    "return 50 Internal service error" when {
      "GitHub API server error occurs" in {
        val apiError = APIError.BadAPIResponse(500, "Could not connect to API.")
        val testUserName = s"${testUserDataModel._id}"
        val testRepoName = "TestRepoName"
        val testUrl = s"https://api.github.com/repos/$testUserName/$testRepoName/contents"
        (mockConnector.get(_: String)(_: Reads[Seq[RepoContentItem]], _: ExecutionContext))
          .expects(testUrl, *, *)
          .returning(EitherT.leftT(apiError))
          .once()

        val getUserObjResult = TestControllerMockServices.getUserRepoContent(testUserName, testRepoName)(FakeRequest())
        status(getUserObjResult) shouldBe INTERNAL_SERVER_ERROR
      }
    }
  }


  "ApplicationController .getUserRepoDirContent" should {
    "return status OK with list of files / directories" when {
      "the user, repo and directory are all valid" in {
        val testUserName = s"${testUserDataModel._id}"
        val testRepoName = "RepoName"
        val testPath = "test/path"
        val testUrl = s"https://api.github.com/repos/$testUserName/$testRepoName/contents/$testPath"
        val testDirContent = Seq(RepoContentItem("Test File Name", "testFilePath", "testSha","file"), RepoContentItem("Test Dir Name", "testDirPath","testSha", "dir"))
        (mockConnector.get(_: String)(_: Reads[Seq[RepoContentItem]], _: ExecutionContext))
          .expects(testUrl, *, *)
          .returning(EitherT.rightT(testDirContent))
          .once()

        val encodedPath = gitService.baseEncodePath(testPath)
        val getUserObjResult = TestControllerMockServices.getUserRepoDirContent(testUserName, testRepoName, encodedPath)(FakeRequest())
        status(getUserObjResult) shouldBe OK
      }
    }
    "return status 404 Not Found" when {
      "the user doesn't exist" in {
        val apiError = APIError.BadAPIResponse(404, "Not Found")
        val testBadUserName = s"Invalid Username"
        val testRepoName = "TestRepoName"
        val testPath = "test/path"
        val encodedPath = gitService.baseEncodePath(testPath)
        val testUrl = s"https://api.github.com/repos/$testBadUserName/$testRepoName/contents/$testPath"

        (mockConnector.get(_: String)(_: Reads[Seq[RepoContentItem]], _: ExecutionContext))
          .expects(testUrl, *, *)
          .returning(EitherT.leftT(apiError))
          .once()

        val getUserObjResult = TestControllerMockServices.getUserRepoDirContent(testBadUserName, testRepoName, encodedPath)(FakeRequest())
        status(getUserObjResult) shouldBe NOT_FOUND
      }
      "the repo doesn't exist" in {
        val apiError = APIError.BadAPIResponse(404, "Not Found")
        val testUserName = s"${testUserDataModel._id}"
        val testBadRepoName = "Invalid Repo Name"
        val testPath = "test/path"
        val encodedPath = gitService.baseEncodePath(testPath)
        val testUrl = s"https://api.github.com/repos/$testUserName/$testBadRepoName/contents/$testPath"

        (mockConnector.get(_: String)(_: Reads[Seq[RepoContentItem]], _: ExecutionContext))
          .expects(testUrl, *, *)
          .returning(EitherT.leftT(apiError))
          .once()

        val getUserObjResult = TestControllerMockServices.getUserRepoDirContent(testUserName, testBadRepoName, encodedPath)(FakeRequest())
        status(getUserObjResult) shouldBe NOT_FOUND
      }
      "the repo directory doesn't exist" in {
        val apiError = APIError.BadAPIResponse(404, "Not Found")
        val testUserName = s"${testUserDataModel._id}"
        val testBadRepoName = "Invalid Repo Name"
        val testBadPath = "Invalid TestPath"
        val encodedBadPath = gitService.baseEncodePath(testBadPath)
        val testUrl = s"https://api.github.com/repos/$testUserName/$testBadRepoName/contents/$testBadPath"

        (mockConnector.get(_: String)(_: Reads[Seq[RepoContentItem]], _: ExecutionContext))
          .expects(testUrl, *, *)
          .returning(EitherT.leftT(apiError))
          .once()

        val getUserObjResult = TestControllerMockServices.getUserRepoDirContent(testUserName, testBadRepoName, encodedBadPath)(FakeRequest())
        status(getUserObjResult) shouldBe NOT_FOUND
      }
    }
    "return status 500 Internal server error" when {
      "GitHub API server error occurs" in {
        val apiError = APIError.BadAPIResponse(500, "Could not connect to API.")
        val testUserName = s"${testUserDataModel._id}"
        val testRepoName = "RepoName"
        val testPath = "TestPath"
        val encodedPath = gitService.baseEncodePath(testPath)
        val testUrl = s"https://api.github.com/repos/$testUserName/$testRepoName/contents/$testPath"

        (mockConnector.get(_: String)(_: Reads[Seq[RepoContentItem]], _: ExecutionContext))
          .expects(testUrl, *, *)
          .returning(EitherT.leftT(apiError))
          .once()

        val getUserObjResult = TestControllerMockServices.getUserRepoDirContent(testUserName, testRepoName, encodedPath)(FakeRequest())
        status(getUserObjResult) shouldBe INTERNAL_SERVER_ERROR
      }
    }
  }


  "ApplicationController .getUserRepoFileContent" should {
    "return status OK with file content" when {
      "the user, repo and file are all valid" in {
        val testUserName = s"${testUserDataModel._id}"
        val testRepoName = "Invalid Repo Name"
        val testPath = "test/path"
        val testSha = "test-sha"
        val encodedPath = gitService.baseEncodePath(testPath)
        val testUrl = s"https://api.github.com/repos/$testUserName/$testRepoName/contents/$testPath"
        val testFileContent = RepoFileItem(name = "Test File", path = "testFilePath", content = "testContentEncoded", encoding = "base64")
        (mockConnector.get(_: String)(_: OFormat[RepoFileItem], _: ExecutionContext))
          .expects(testUrl, *, *)
          .returning(EitherT.rightT(testFileContent))
          .once()

        val getUserObjResult = TestControllerMockServices.getUserRepoFileContent(testUserName, testRepoName, encodedPath, testSha)(FakeRequest())
        status(getUserObjResult) shouldBe OK
      }
    }
    "return status 404 Not Found" when {
      "the user doesn't exist" in {
        val apiError = APIError.BadAPIResponse(404, "Not Found")
        val testBadUserName = s"Invalid Username"
        val testRepoName = "TestRepoName"
        val testPath = "TestFilePath"
        val testSha = "test-sha"
        val encodedPath = gitService.baseEncodePath(testPath)
        val testUrl = s"https://api.github.com/repos/$testBadUserName/$testRepoName/contents/$testPath"

        (mockConnector.get(_: String)(_: OFormat[RepoFileItem], _: ExecutionContext))
          .expects(testUrl, *, *)
          .returning(EitherT.leftT(apiError))
          .once()

        val getUserObjResult = TestControllerMockServices.getUserRepoFileContent(testBadUserName, testRepoName, encodedPath, testSha)(FakeRequest())
        status(getUserObjResult) shouldBe NOT_FOUND
      }
      "the repo doesn't exist" in {
        val apiError = APIError.BadAPIResponse(404, "Not Found")
        val testUserName = s"${testUserDataModel._id}"
        val testBadRepoName = "Invalid Repo Name"
        val testPath = "TestPath"
        val encodedPath = gitService.baseEncodePath(testPath)
        val testUrl = s"https://api.github.com/repos/$testUserName/$testBadRepoName/contents/$testPath"

        (mockConnector.get(_: String)(_: Reads[Seq[RepoContentItem]], _: ExecutionContext))
          .expects(testUrl, *, *)
          .returning(EitherT.leftT(apiError))
          .once()

        val getUserObjResult = TestControllerMockServices.getUserRepoDirContent(testUserName, testBadRepoName, encodedPath)(FakeRequest())
        status(getUserObjResult) shouldBe NOT_FOUND
      }
      "the repo directory doesn't exist" in {
        val apiError = APIError.BadAPIResponse(404, "Not Found")
        val testUserName = s"${testUserDataModel._id}"
        val testRepoName = "Invalid Repo Name"
        val testBadPath = "Invalid TestFilePath"
        val testSha = "test-sha"
        val encodedBadPath = gitService.baseEncodePath(testBadPath)
        val testUrl = s"https://api.github.com/repos/$testUserName/$testRepoName/contents/$testBadPath"

        (mockConnector.get(_: String)(_: OFormat[RepoFileItem], _: ExecutionContext))
          .expects(testUrl, *, *)
          .returning(EitherT.leftT(apiError))
          .once()

        val getUserObjResult = TestControllerMockServices.getUserRepoFileContent(testUserName, testRepoName, encodedBadPath, testSha)(FakeRequest())
        status(getUserObjResult) shouldBe NOT_FOUND
      }
    }
    "return status 500 Internal server error" when {
      "GitHub API server error occurs" in {
        val apiError = APIError.BadAPIResponse(500, "Could not connect to API.")
        val testUserName = s"${testUserDataModel._id}"
        val testRepoName = "RepoName"
        val testPath = "TestFilePath"
        val encodedPath = gitService.baseEncodePath(testPath)
        val testUrl = s"https://api.github.com/repos/$testUserName/$testRepoName/contents/$testPath"

        (mockConnector.get(_: String)(_: OFormat[RepoFileItem], _: ExecutionContext))
          .expects(testUrl, *, *)
          .returning(EitherT.leftT(apiError))
          .once()

        val getUserObjResult = TestControllerMockServices.getUserRepoDirContent(testUserName, testRepoName, encodedPath)(FakeRequest())
        status(getUserObjResult) shouldBe INTERNAL_SERVER_ERROR
      }
    }
  }

  "ApplicationController .getUsernameSearch" should {

    "return OK and render the searchUsername view" in {

      val result = TestController.getUsernameSearch()(FakeRequest().withCSRFToken)

      status(result) shouldBe  OK
      contentAsString(result) should include("Search Username Form")
    }
  }

  "ApplicationController .getUsernameSearchResult" should {

    "return BadRequest and re-render the form when the form data is invalid" in {

      val result: Future[Result] = TestController.getUsernameSearchResult()(FakeRequest().withCSRFToken)


      status(result) shouldBe BAD_REQUEST
      contentAsString(result) should include("Search Username Form")

    }
  }

  override def beforeEach(): Unit = await(repository.deleteAll())

  override def afterEach(): Unit = await(repository.deleteAll())
}
