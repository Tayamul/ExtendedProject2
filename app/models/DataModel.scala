package models

import play.api.libs.json.{Json, OFormat}

// _id relates to username
case class DataModel(_username: String,
                     dateCreated: String,
                     location: String,
                     numFollowers: Int,
                     numFollowing: Int)

object DataModel {
  implicit val formats: OFormat[DataModel] = Json.format[DataModel]
}