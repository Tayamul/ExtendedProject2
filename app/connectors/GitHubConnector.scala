package connectors

import com.google.inject._
import play.api.libs.ws.WSClient

@Singleton
class GitHubConnector @Inject()(ws: WSClient) {

}
