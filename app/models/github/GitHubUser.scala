package models.github

import play.api.libs.json.{Json, OFormat}
import play.api.data._
import play.api.data.Forms._

case class GitHubUser(
                     login: String,
                     location: Option[String],
                     followers: Int,
                     following: Int,
                     created_at: String,
                     repos_url: String,
                     name: Option[String],
                     avatar_url: String,
                     blog: Option[String],
                     bio: Option[String]
                     )
object GitHubUser {
  implicit val formats: OFormat[GitHubUser] = Json.format[GitHubUser]

  val userForm: Form[GitHubUser] = Form(
    mapping(
      "login" -> text,
      "location" -> optional(text),
      "followers" -> number,
      "following" -> number,
      "created_at" -> text,
      "repos_url" -> text,
      "name" -> optional(text),
      "avatar_url" -> text,
      "blog" -> optional(text),
      "bio" -> optional(text)
    )(GitHubUser.apply)(GitHubUser.unapply)
  )
}


// EXAMPLE RESPONSE & SCHEMA INFORMATION:
// https://docs.github.com/en/rest/users/users?apiVersion=2022-11-28#list-users