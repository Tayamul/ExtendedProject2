package connectors

import cats.data.EitherT
import cats.data.EitherT.rightT
import com.google.inject._
import models.{APIError, GitHubUser}
import play.api.libs.json.{JsError, JsSuccess, OFormat}
import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GitHubConnector @Inject()(ws: WSClient) {

  def getUserByUserName[GitHubUser](url: String)(implicit rds: OFormat[GitHubUser], ec: ExecutionContext): Future[Either[APIError, GitHubUser]] = {
    val request = ws.url(url)
    val response = request.get()

    response.map { result =>
      val json = result.json
      json.validate[GitHubUser] match {
        case JsSuccess(user, _) => Right(user)
        case JsError(errors) => Left(APIError.BadAPIResponse(500, s"Error parsing JSON response. Message: ${errors}"))
      }
    }.recover { case _: WSResponse =>
      Left(APIError.BadAPIResponse(500, "Could not connect to the database."))
    }
  }

}
