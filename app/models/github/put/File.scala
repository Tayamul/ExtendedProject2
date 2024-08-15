package models.github.put

import play.api.libs.json.{Json, OFormat}

case class File(
                       message: String,
                       content: String,
                       branch: Option[String] = None,
                       committer: Option[Commiter] = None,
                       author: Option[Commiter] = None
                     )

object File {
  implicit val formats: OFormat[File] = Json.format[File]
}