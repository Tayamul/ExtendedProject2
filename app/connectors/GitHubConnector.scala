package connectors

import cats.data.EitherT
import com.google.inject._
import play.api.libs.ws.WSClient

@Singleton
class GitHubConnector @Inject()(ws: WSClient) {

//  def getUserByUserName[Response](url: String)
}
