package models.forms

import models.github.put.{Commiter, CreateFile}
import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText, optional, text}
import play.api.libs.json.{Json, OFormat}

case class DeleteFileForm (
                            message: String,
                            branch: Option[String] = None,
                            committerName: Option[String] = None,
                            committerEmail: Option[String] = None,
                            authorName: Option[String] = None,
                            authorEmail: Option[String] = None,
                          )


object DeleteFileForm {

  implicit val formats: OFormat[DeleteFileForm] = Json.format[DeleteFileForm]
  // TODO make the branches a select option from possible branches
  val form: Form[DeleteFileForm] = Form {
    mapping(
      "message" -> nonEmptyText,
      "branch" -> optional(text),
      "committerName" -> optional(text),
      "committerEmail" -> optional(text),
      "authorName" -> optional(text),
      "authorEmail" -> optional(text),
    )(DeleteFileForm.apply)(DeleteFileForm.unapply)
  }
}
