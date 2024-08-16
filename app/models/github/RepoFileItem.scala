package models.github

import play.api.libs.json.{Json, OFormat}

case class RepoFileItem(
                         name: String,
                         path: String,
                         content: String,
                         encoding:String
                       )
object RepoFileItem{
  implicit val formats: OFormat[RepoFileItem] = Json.format[RepoFileItem]
}