package controllers

import baseSpec.BaseSpecWithApplication
import org.scalamock.scalatest.MockFactory

class ApplicationControllerSpec extends BaseSpecWithApplication with MockFactory {

  val TestController = new ApplicationController(
    component,
    repoService,
    gitService
  )(executionContext)

}
