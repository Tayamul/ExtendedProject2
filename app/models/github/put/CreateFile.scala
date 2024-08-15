package models.github.put

import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText, optional, text}
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

  private val committerMapping = mapping(
    "name" -> nonEmptyText,
    "email" -> nonEmptyText
  )(Commiter.apply)(Commiter.unapply)


  // TODO make the branches a select option from possible branches
  val createForm: Form[CreateFile] = Form {
    mapping(
      "messages" -> nonEmptyText,
      "content" -> nonEmptyText,
      "branch" -> optional(text),
      "committer" -> optional(committerMapping),
      "author" -> optional(committerMapping)
    )(CreateFile.apply)(CreateFile.unapply)
  }

}