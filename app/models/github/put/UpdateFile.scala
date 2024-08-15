package models.github.put

import play.api.libs.json.{Json, OFormat}

case class UpdateFile (
                        message: String,
                        content: String,
                        sha: String,
                        branch: Option[String] = None,
                        committer: Option[Commiter] = None,
                        author: Option[Commiter] = None
                      )

object UpdateFile {
  implicit val formats: OFormat[UpdateFile] = Json.format[UpdateFile]
}