package models

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

case class GitHubUser(
                     login: String,
                     location: Option[String],
                     followers: Int,
                     following: Int,
                     created_at: String,
                     repos_url: String,
                     name: Option[String]
                     )
object GitHubUser {
  implicit val formats: OFormat[GitHubUser] = Json.format[GitHubUser]
}


// EXAMPLE RESPONSE & SCHEMA INFORMATION:
// https://docs.github.com/en/rest/users/users?apiVersion=2022-11-28#list-users