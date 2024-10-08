package models.forms

import models.github.put.{Commiter, CreateFile}
import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText, optional, text}
import play.api.libs.json.{Json, OFormat}

case class CreateFileForm (
                          name:String,
                          message: String,
                          content: String,
                          branch: Option[String] = None,
                          committerName: Option[String] = None,
                          committerEmail: Option[String] = None,
                          authorName: Option[String] = None,
                          authorEmail: Option[String] = None,
                          )


object CreateFileForm{

  implicit val formats: OFormat[CreateFileForm] = Json.format[CreateFileForm]
  // TODO make the branches a select option from possible branches
  val form: Form[CreateFileForm] = Form {
    mapping(
      "name" -> nonEmptyText,
      "message" -> nonEmptyText,
      "content" -> nonEmptyText,
      "branch" -> optional(text),
      "committerName" -> optional(text),
      "committerEmail" -> optional(text),
      "authorName" -> optional(text),
      "authorEmail" -> optional(text),
    )(CreateFileForm.apply)(CreateFileForm.unapply)
  }
}
