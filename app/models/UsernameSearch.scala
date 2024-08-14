package models

import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText, text}
import play.api.libs.json.{Json, OFormat}

case class UsernameSearch(
                         username: String
                         )
object UsernameSearch{
  implicit val formats: OFormat[UsernameSearch] = Json.format[UsernameSearch]

  val username: Form[UsernameSearch] = Form {
    mapping(
      "username" -> nonEmptyText(minLength = 3, maxLength = 39)
        .verifying("Username can only contain alphanumeric characters or hyphens", _.matches("^[a-zA-Z0-9-]+$"))
    )(UsernameSearch.apply)(UsernameSearch.unapply)
  }
}
