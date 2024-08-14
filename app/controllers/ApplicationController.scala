package controllers

import cats.data.EitherT
import models.APIError.BadAPIResponse
import models.{APIError, DataModel, UsernameSearch}
import play.api.data.Form
import play.api.data.Forms.{nonEmptyText, single}
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents, Request, Result}
import services.{GitHubService, RepositoryService}
import views.html.helper.CSRF

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ApplicationController @Inject()(
                                       val controllerComponents: ControllerComponents,
                                       val repoService: RepositoryService,
                                       val gitHubService: GitHubService
                                     )(implicit val ec: ExecutionContext) extends BaseController {


  // convert api errors to Status result
  private def resultError(error: APIError): Result = {
    error match {
      case BadAPIResponse(upstreamStatus, upstreamMessage) => Status(upstreamStatus)(Json.toJson(upstreamMessage))
      case _ => Status(error.httpResponseStatus)(Json.toJson(error.reason))
    }
  }


  /** ---- REPO SERVICE CRUD OPERATIONS ---- */

  def index(): Action[AnyContent] = Action.async { implicit request =>
    repoService.index().map {
      // TODO implement a no items in database view? - error 404 or 500
      case Left(error) => Status(error.upstreamStatus)(Json.toJson(error.upstreamMessage))

      case Right(users: Seq[DataModel]) => Ok {
        Json.toJson(users)
      }
    }
  }

  /** ---- REPO SERVICE CRUD OPERATIONS ---- */
  def create(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[DataModel] match {
      case JsError(_) => Future(BadRequest)
      // _ = JsPath
      case JsSuccess(dataModel, _) =>
        repoService.create(dataModel).map {
          case Right(createdDataModel) => Created(Json.toJson(createdDataModel))
          case Left(error) => resultError(error)
        }
    }
  }

  def read(username: String): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    repoService.read(username).map {
      case Left(error) => Status(error.upstreamStatus)(Json.toJson(error.upstreamMessage))
      case Right(item: DataModel) => Ok(Json.toJson(item))
    }
  }

  def update(username: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
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

  def getGitHubUser(username: String): Action[AnyContent] = Action.async { request =>
    gitHubService.getUserByUserName(username = username).value.map {
      case Left(error) => resultError(error)
      case Right(user) => Ok {
        Json.toJson(user)
      }
    }
  }

  def getUserObj(username: String): Action[AnyContent] = Action.async { implicit result =>
    gitHubService.getUserObjToStore(username = username).value.flatMap {
      case Right(user) =>
        repoService.createUserObjToStore(user).map {
          case Right(_) => Ok(Json.toJson(user))
          case Left(error) => resultError(error)
        }
      case Left(error) => Future.successful(resultError(error))
    }
  }

  def getUserRepos(username: String): Action[AnyContent] = Action.async { request =>
    gitHubService.getUserRepos(None, username).value.map {
      case Left(error) => resultError(error)
      case Right(repos) => Ok {
        Json.toJson(repos)
      }
    }
  }

  def getUserRepoByRepoName(username: String, repoName: String): Action[AnyContent] = Action.async { result =>
    gitHubService.getUserRepoByRepoName(None, username, repoName).value.map {
      case Left(error) => resultError(error)
      case Right(repos) => Ok {
        Json.toJson(repos)
      }
    }
  }


  def getUserRepoContent(username: String, repoName: String): Action[AnyContent] = Action.async { result =>
    gitHubService.getUserRepoContent(None, username, repoName).value.map {
      case Left(error) => resultError(error)
      case Right(repoContent) => Ok {
        Json.toJson(repoContent)
      }
    }
  }


  def getUserRepoDirContent(username: String, repoName: String, path: String): Action[AnyContent] = Action.async { result =>
    gitHubService.getUserRepoDirContent(None, username, repoName, path).value.map {
      case Left(error) => resultError(error)
      case Right(repoContent) => Ok {
        Json.toJson(repoContent)
      }
    }
  }


  def getUserRepoFileContent(username: String, repoName: String, path: String): Action[AnyContent] = Action.async { result =>
    gitHubService.getUserRepoFileContent(None, username, repoName, path).value.map {
      case Left(error) => resultError(error)
      case Right(repoContent) =>
        val plainTextContent = gitHubService.convertContentToPlainText(repoContent.content)
        Ok {
          Json.toJson(plainTextContent)
        }
    }
  }


  /** ---- Form Rendering ---- */

  // Remember to call accessToken in render methods
  private def accessToken(implicit request: Request[_]) = {
    CSRF.getToken
  }

  def getUserNameSearch(): Action[AnyContent] = ???


  /** ---- Form Submission Redirects ---- */


  def getUsernameSearchResult: Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    UsernameSearch.usernameSearchForm.bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest), // Show form with errors
      usernameSearch => {
        gitHubService.getUserByUserName(username = usernameSearch.username).value.map {
          case Left(error) => resultError(error)
          case Right(user) => Ok {
            Json.toJson(user)
          }
        }
      }
    )
  }

}
