package controllers

import models.DataModel
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents, Request}

import services.RepositoryService
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ApplicationController @Inject()(
                                       val controllerComponents: ControllerComponents,
                                       val repoService: RepositoryService
                                     )(implicit val ec: ExecutionContext)extends BaseController {


  def index(): Action[AnyContent] = ???


  def create(): Action[JsValue] = ???


  def read(id: String): Action[AnyContent] = ???


  def update():Action[AnyContent] = ???


  def delete():Action[AnyContent] = ???


  def getGitHubUser():Action[AnyContent] = ???

}
