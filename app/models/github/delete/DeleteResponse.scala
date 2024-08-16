package models.github.delete

import models.github.Commit
import play.api.libs.json.{Json, OFormat}

case class DeleteResponse(
                        commit: Commit
                      )

object DeleteResponse {
  implicit val formats: OFormat[DeleteResponse] = Json.format[DeleteResponse]
}