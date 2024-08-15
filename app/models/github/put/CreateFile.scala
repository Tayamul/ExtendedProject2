package models.github.put

import play.api.libs.json.{Json, OFormat}

case class CreateFile(
                       message: String,
                       content: String,
                       branch: Option[String] = None,
                       committer: Option[Commiter] = None,
                       author: Option[Commiter] = None
                     )

object CreateFile {
  implicit val formats: OFormat[CreateFile] = Json.format[CreateFile]
}