package connectors

import baseSpec.BaseSpec
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext

class GitHubConnectorSpec extends BaseSpec with ScalaFutures with MockFactory with GuiceOneServerPerSuite {

  val mockWSClient: WSClient = mock[WSClient]
  val testConnector: GitHubConnector = new GitHubConnector(mockWSClient)
  implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]

}
