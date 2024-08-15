package models.github.put

import models.github.RepoFileItem
import play.api.libs.json.{Json, OFormat}

case class Commiter(
                     name: String,
                     email: String
                   )

object Commiter {
  implicit val formats: OFormat[RepoFileItem] = Json.format[RepoFileItem]
}