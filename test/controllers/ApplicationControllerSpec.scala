package controllers

import baseSpec.BaseSpecWithApplication
import models.DataModel
import org.scalamock.scalatest.MockFactory
import play.api.test.Helpers.{await, defaultAwaitTimeout}
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

  "Application Controller .index" should {
    "return 200 Ok with body" in {

    }
  }

  override def beforeEach(): Unit = await(repository.deleteAll())
  override def afterEach(): Unit = await(repository.deleteAll())
}
