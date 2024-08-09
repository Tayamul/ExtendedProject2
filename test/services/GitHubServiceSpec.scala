package services

import baseSpec.BaseSpec
import connectors.GitHubConnector
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.guice.GuiceOneServerPerSuite

import scala.concurrent.ExecutionContext

class GitHubServiceSpec extends BaseSpec with ScalaFutures with MockFactory with GuiceOneServerPerSuite {

  val mockConnector: GitHubConnector = mock[GitHubConnector]
  implicit val ec: ExecutionContext = app.injector.asInstanceOf[ExecutionContext]
  val testService: GitHubService = new GitHubService(mockConnector)

}
