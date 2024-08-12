package controllers

import baseSpec.BaseSpecWithApplication
import models.{APIError, DataModel}
import org.scalamock.scalatest.MockFactory
import play.api.http.Status
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContent, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, contentAsJson, defaultAwaitTimeout, status}
import services.RepositoryService

import scala.concurrent.Future

class ApplicationControllerSpec extends BaseSpecWithApplication with MockFactory {

  val TestController = new ApplicationController(
    component,
    repoService,
    gitService
  )(executionContext)


  val userTestDataModel: DataModel = DataModel(
    _username = "testUserName",
    dateCreated = "2020-10-16T09:59:16Z",
    location = "London",
    numFollowers = 1,
    numFollowing = 2,
    repoUrl = "https://api.github.com/users/tbg2003/repos",
    name = "Test User"
  )

  "ApplicationController .index" should {
    "return 200 Ok with body" when {
      "Items in database" in {
        beforeEach()

        val createUserRequest:FakeRequest[JsValue] = testRequest.buildPost("api/user").withBody[JsValue](Json.toJson(userTestDataModel))
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
      val request: FakeRequest[JsValue] = testRequest.buildPost("/api/user").withBody[JsValue](Json.toJson(userTestDataModel))
      val createdResult: Future[Result] = TestController.create()(request)

      status(createdResult) shouldBe Status.CREATED
      contentAsJson(createdResult).as[DataModel] shouldBe userTestDataModel
      afterEach()
    }

    "return 500 Internal Server Error" in {
      beforeEach()

      val apiError:APIError = APIError.BadAPIResponse(500, "An error has occurred:")

      val request:FakeRequest[JsValue] = testRequest.buildPost("/api/user").withBody[JsValue](Json.toJson(userTestDataModel))
      val request2:FakeRequest[JsValue] = testRequest.buildPost("/api/user").withBody[JsValue](Json.toJson(userTestDataModel))

      val createdResult: Future[Result] = TestController.create()(request)
//      status(createdResult) shouldBe CREATED


      val createdResult2: Future[Result] = TestController.create()(request2)
      status(createdResult2) shouldBe apiError.httpResponseStatus

      afterEach()
    }

    "return 400 Bad Request" in {
      beforeEach()
      val badRequestBody:JsValue = Json.parse("""{"username": "abcd", "name": 12345}""")
      val badRequest: FakeRequest[JsValue] = testRequest.buildPost("/api/user").withBody[JsValue](Json.toJson(badRequestBody))
      val createdResult: Future[Result] = TestController.create()(badRequest)

      status(createdResult) shouldBe Status.BAD_REQUEST
      afterEach()
    }
  }

  "ApplicationController .read" should {
    "return 200 Ok with body" in {
      beforeEach()

      val request: FakeRequest[JsValue] = testRequest.buildPost("/api/user").withBody[JsValue](Json.toJson(userTestDataModel))
      val createdResult: Future[Result] = TestController.create()(request)

      status(createdResult) shouldBe Status.CREATED

      val readResult: Future[Result] = TestController.read("testUserName")(FakeRequest())

      status(readResult) shouldBe Status.OK
      contentAsJson(readResult).as[DataModel] shouldBe userTestDataModel

      afterEach()
    }

    "return 404 Not Found with body" in {
      beforeEach()

      val request: FakeRequest[JsValue] = testRequest.buildPost("/api/user").withBody[JsValue](Json.toJson(userTestDataModel))
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

      val createRequest: FakeRequest[JsValue] = testRequest.buildPost("/api/user").withBody[JsValue](Json.toJson(userTestDataModel))
      val createdResult: Future[Result] = TestController.create()(createRequest)
      status(createdResult) shouldBe Status.CREATED

      val updateRequest: FakeRequest[JsValue] = testRequest.buildPut("/api/user").withBody[JsValue](Json.toJson(userTestDataModel))
      val updateResult: Future[Result] = TestController.update("testUserName")(updateRequest)

      status(updateResult) shouldBe Status.ACCEPTED
      contentAsJson(updateResult).as[DataModel] shouldBe userTestDataModel

      afterEach()
    }

    "return 500 Internal Server Error" in {
      beforeEach()

      val apiError:APIError = APIError.BadAPIResponse(500, "error message")

      val createRequest: FakeRequest[JsValue] = testRequest.buildPost("/api/user").withBody[JsValue](Json.toJson(userTestDataModel))
      val createdResult: Future[Result] = TestController.create()(createRequest)
//      status(createdResult) shouldBe Status.CREATED

      val updateRequest: FakeRequest[JsValue] = testRequest.buildPut("/api/user").withBody[JsValue](Json.toJson(userTestDataModel))
      val updateResult: Future[Result] = TestController.update("nonExistingUserName")(updateRequest)

      status(updateResult) shouldBe apiError.httpResponseStatus

      afterEach()
    }

    "return 400 Bad Request" in {
      beforeEach()

      val request: FakeRequest[JsValue] = testRequest.buildPost("/api/update/${userTestDataModel._username}").withBody[JsValue](Json.toJson(userTestDataModel))
      val createdResult: Future[Result] = TestController.create()(request)
      status(createdResult) shouldBe Status.CREATED


      val badRequestBody:JsValue = Json.parse("""{"id": "testUserName", "name": 12345}""")
      val badUpdateRequest: FakeRequest[JsValue] = testRequest.buildPut("/api/user").withBody[JsValue](Json.toJson(badRequestBody))
      val updateResult: Future[Result] = TestController.update("testUserName")(badUpdateRequest)

      status(updateResult) shouldBe Status.BAD_REQUEST

      afterEach()
    }
  }

  "ApplicationController .delete"

  override def beforeEach(): Unit = await(repository.deleteAll())
  override def afterEach(): Unit = await(repository.deleteAll())
}
