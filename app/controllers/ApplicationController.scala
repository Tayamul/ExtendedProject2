package controllers

import cats.data.EitherT
import models.APIError.BadAPIResponse
import models.{APIError, DataModel}
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents, Request, Result}
import services.{GitHubService, RepositoryService}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ApplicationController @Inject()(
                                       val controllerComponents: ControllerComponents,
                                       val repoService: RepositoryService,
                                       val gitHubService: GitHubService
                                     )(implicit val ec: ExecutionContext) extends BaseController {

  // convert api errors to Status result
  private def resultError(error:APIError):Result = {
    error match {
      case BadAPIResponse(upstreamStatus, upstreamMessage) => Status(upstreamStatus)(Json.toJson(upstreamMessage))
      case _ => Status(error.httpResponseStatus)(Json.toJson(error.reason))
    }
  }


/** ---- REPO SERVICE CRUD OPERATIONS ---- */

  def index(): Action[AnyContent] = Action.async { implicit request =>
    repoService.index().map{
      case Left(error) => Status(error.upstreamStatus)(Json.toJson(error.upstreamMessage))
      // TODO implement a no items in database view?
      case Right(item: Seq[DataModel]) => Ok {Json.toJson(item)}
    }
  }


  def create(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[DataModel] match {
      case JsError(_) => Future(BadRequest)
      // _ = JsPath
      case JsSuccess(dataModel, _) =>
        repoService.create(dataModel).map{
          case Right(createdDataModel) => Created(Json.toJson(createdDataModel))
          case Left(error) => resultError(error)
        }
    }
  }


  def read(username: String): Action[AnyContent] = Action.async{implicit request: Request[AnyContent] =>
    repoService.read(username).map {
      case Left(error) => Status(error.upstreamStatus)(Json.toJson(error.upstreamMessage))
      case Right(item: DataModel) => Ok(Json.toJson(item))
    }
  }


  def update(username: String): Action[JsValue] = Action.async(parse.json){implicit request =>
    request.body.validate[DataModel] match {
      case JsError(errors) => Future(BadRequest)
      case JsSuccess(dataModel, _) =>
        repoService.update(username, dataModel).map {
          case Left(error) => resultError(error)
          case Right(result) => Accepted(Json.toJson(dataModel))
        }
    }
  }


  def delete(username: String): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    repoService.delete(username).map {
      case Left(error) => resultError(error)
      case Right(result) => Accepted(Json.toJson(s"Successfully Deleted User: $username"))
    }
  }


  /** ---- GITHUB SERVICE OPERATIONS ---- */

  def getGitHubUser(username: String):Action[AnyContent] = Action.async { request =>
    gitHubService.getUserByUserName(username=username).value.map {
      case Left(error) => resultError(error)
      case Right(user) => Ok{Json.toJson(user)}
    }
  }
}
