package models.github.delete

import models.github.put.Commiter
import play.api.libs.json.{Json, OFormat}

case class DeleteFile (
                      message: String,
                      sha: String,
                      branch: String,
                      committer: Option[Commiter] = None,
                      author: Option[Commiter] = None
                      )

object DeleteFile {
  implicit val format: OFormat[DeleteFile] = Json.format[DeleteFile]
}