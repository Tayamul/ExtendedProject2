package models

import play.api.libs.json.{Json, OFormat}
import java.time.LocalDate

case class GitHubUser(login: String,
                      id: Int,
                      node_id: String,
                      avatar_url: String,
                      gravatar_id: String,
                      url: String,
                      html_url: String,
                      followers_url: String,
                      following_url: String,
                      gists_url: String,
                      starred_url: String,
                      subscriptions_url: String,
                      organizations_url: String,
                      repos_url: String,
                      events_url: String,
                      received_events_url: String,
                      site_admin: Boolean,
                      name: String,
                      company: Option[String],
                      blog: String,
                      location: Option[String],
                      email: Option[String],
                      hireable: Option[String],
                      bio: Option[String],
                      twitter_username: Option[String],
                      public_repos: Int,
                      public_gists: Int,
                      followers: Int,
                      following: Int,
                      created_at: LocalDate,
                      updated_at: LocalDate)

object GitHubUser {
  implicit val formats: OFormat[GitHubUser] = Json.format[GitHubUser]
}