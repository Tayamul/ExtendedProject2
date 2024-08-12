package controllers

import baseSpec.BaseSpecWithApplication
import models.DataModel
import org.scalamock.scalatest.MockFactory
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, defaultAwaitTimeout, status}
import services.RepositoryService

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

  override def beforeEach(): Unit = await(repository.deleteAll())
  override def afterEach(): Unit = await(repository.deleteAll())
}
