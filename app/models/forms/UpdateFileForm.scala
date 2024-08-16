package models.forms

import models.github.put.Commiter
import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText, optional, text}
import play.api.libs.json.{Json, OFormat}

case class UpdateFileForm(
                           message: String,
                           content: String,
                           sha: String,
                           committerName: Option[String] = None,
                           committerEmail: Option[String] = None,
                           authorName: Option[String] = None,
                           authorEmail: Option[String] = None,
                         )
object UpdateFileForm{

  implicit val formats: OFormat[UpdateFileForm] = Json.format[UpdateFileForm]
  // TODO make the branches a select option from possible branches
  val form: Form[UpdateFileForm] = Form {
    mapping(
      "message" -> nonEmptyText,
      "content" -> nonEmptyText,
      "sha" -> nonEmptyText,
      "committerName" -> optional(text),
      "committerEmail" -> optional(text),
      "authorName" -> optional(text),
      "authorEmail" -> optional(text),
    )(UpdateFileForm.apply)(UpdateFileForm.unapply)
  }
}