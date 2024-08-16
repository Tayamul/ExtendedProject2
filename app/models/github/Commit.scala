package models.github

import play.api.libs.json.{Json, OFormat}

case class Commit(
                     sha: String
                   )

object Commit {
  implicit val formats: OFormat[Commit] = Json.format[Commit]
}