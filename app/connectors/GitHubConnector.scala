package connectors

import cats.data.EitherT
import cats.data.EitherT.rightT
import com.google.inject._
import models.error._
import play.api.libs.json.{JsError, JsSuccess, Json, OFormat, Reads}
import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.{ExecutionContext, Future}
import com.typesafe.config.ConfigFactory
import models.github.delete.DeleteFile
import models.github.put.{CreateFile, UpdateFile}

import javax.swing.text.html.HTML

@Singleton
class GitHubConnector @Inject()(ws: WSClient) {

  private val personalAccessToken = ConfigFactory.load().getString("play.http.secret.key")

  def getHTML(url: String)(implicit ec: ExecutionContext): EitherT[Future, APIError, String] = {

    val request = ws.url(url).addHttpHeaders(
      "Accept" -> "application/vnd.github.html+json",
      "Authorization" -> s"Bearer $personalAccessToken")

    val response = request.get()

    EitherT {
      response.map { response =>
        response.status match {
          case 200 =>
            Right(response.body)
          case _ =>
            Left(APIError.BadAPIResponse(500,"Could not fetch README"))
        }
      }
    }
  }

  def get[Response](url: String)(implicit rds: Reads[Response], ec: ExecutionContext): EitherT[Future, APIError, Response] = {

    val request = ws.url(url).addHttpHeaders(
      "Accept" -> "application/vnd.github+json",
      "Authorization" -> s"Bearer $personalAccessToken")

    val response = request.get()

    EitherT {
      response.map { result =>
        if (result.status == 200) {
          Right(result.json.as[Response])
        } else {
          Left(APIError.BadAPIResponse(result.status, result.statusText))
        }
      }.recover { case _: WSResponse =>
        Left(APIError.BadAPIResponse(500, "Could not connect to API."))
      }
    }
  }

  def create[Response](url: String, file: CreateFile)(implicit rds: Reads[Response], ec: ExecutionContext): EitherT[Future, APIError, Response] = {

    val request = ws.url(url).addHttpHeaders(
      "Accept" -> "application/vnd.github+json",
      "Authorization" -> s"Bearer $personalAccessToken")

    val response = request.put(Json.toJson(file))

    EitherT {
      response.map { result =>
        if (result.status == 201) {
          Right(result.json.as[Response])
        } else {
          Left(APIError.BadAPIResponse(result.status, result.statusText))
        }
      }.recover { case _: WSResponse =>
        Left(APIError.BadAPIResponse(500, "Could not connect to API."))
      }
    }
  }

  def update[Response](url: String, file: UpdateFile)(implicit rds: Reads[Response], ec: ExecutionContext): EitherT[Future, APIError, Response] = {

    val request = ws.url(url).addHttpHeaders(
      "Accept" -> "application/vnd.github+json",
      "Authorization" -> s"Bearer $personalAccessToken")

    val response = request.put(Json.toJson(file))

    EitherT {
      response.map { result =>
        if (result.status == 200 || result.status == 201) {
          Right(result.json.as[Response])
        } else {
          Left(APIError.BadAPIResponse(result.status, result.statusText))
        }
      }.recover { case _: WSResponse =>
        Left(APIError.BadAPIResponse(500, "Could not connect to API."))
      }
    }
  }

  def delete[Response](url: String, file: DeleteFile)(implicit rds: Reads[Response], ec: ExecutionContext): EitherT[Future, APIError, Response] = {
    val request = ws.url(url).addHttpHeaders(
      "Accept" -> "application/vnd.github+json",
      "Authorization" -> s"Bearer $personalAccessToken")

    val response = request.withMethod("DELETE").withBody(Json.toJson(file)).execute()

    EitherT {
      response.map { result =>
        if (result.status == 200) {
          Right(result.json.as[Response])
        } else {
          Left(APIError.BadAPIResponse(result.status, result.statusText))
        }
      }.recover { case _: WSResponse =>
        Left(APIError.BadAPIResponse(500, "Could not connect to API."))
      }
    }
  }

}
