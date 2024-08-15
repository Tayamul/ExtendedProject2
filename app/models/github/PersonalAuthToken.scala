package models.github

import com.google.inject._
import play.api.Configuration

@Singleton
class PersonalAuthToken @Inject()(config: Configuration) {
  def get:String = config.underlying.getString("play.http.secret.key")
}

//import play.api.Configuration
//import play.api.Environment
//
//object PersonalAuthToken {
//  private val config = Configuration.reference
//
//  def get: String = config.underlying.getString("play.http.secret.key")
//}