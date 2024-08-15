package models.github.put

import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText, optional}
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

  private val committerMapping = mapping(
    "name" -> nonEmptyText,
    "email" -> nonEmptyText
  )(Commiter.apply)(Commiter.unapply)


  // TODO make the branches a select option from possible
  //  add more custom constraints

  val usernameSearchForm: Form[UpdateFile] = Form {
    mapping(
      "messages" -> nonEmptyText,
      "content" -> nonEmptyText,
      "sha" -> nonEmptyText,
      "branch" -> optional(nonEmptyText),
      "committer" -> optional(committerMapping),
      "author" -> optional(committerMapping)
    )(UpdateFile.apply)(UpdateFile.unapply)
  }
}