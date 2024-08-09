package controllers

import models.DataModel
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import repositories.DataRepository

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class ApplicationController @Inject()(
                                       val controllerComponents: ControllerComponents,
                                       val repository: DataRepository
                                     )(implicit val ec: ExecutionContext)extends BaseController {


  def index(): Action[AnyContent] = Action.async { implicit request =>
    repository.index().map{
      case Left(error) => Status(error.upstreamStatus)(error.upstreamMessage)
      case Right(users: Seq[DataModel]) => Ok(Json.toJson(users))
    }
  }


  def create(): Action[AnyContent] = ???


  def read(): Action[AnyContent] = ???


  def update():Action[AnyContent] = ???


  def delete():Action[AnyContent] = ???


  def getGitHubUser():Action[AnyContent] = ???

}
