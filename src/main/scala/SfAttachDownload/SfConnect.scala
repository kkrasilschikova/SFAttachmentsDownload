package SfAttachDownload

import com.sforce.soap.partner.PartnerConnection
import com.sforce.ws.ConnectorConfig
import scala.util.{Failure, Success, Try}

class SfConnect {
  def connect: Try[PartnerConnection] = {
    val servicesEndpointSuffix = "services/Soap/u/40.0/"
    val authEndPoint = "https://login.salesforce.com/" + servicesEndpointSuffix

    val configTry: Try[ConnectorConfig] = {
      val maybeUsername = sys.env.get("SALESFORCE_USERNAME")
      val maybePassword = sys.env.get("SALESFORCE_PASSWORD")
      val usernameTry = maybeUsername
        .fold[Try[String]](Failure(new Error("You must specify the SALESFORCE_USERNAME env var")))(Success(_))
      val passwordTry = maybePassword
        .fold[Try[String]](Failure[String](new Error("You must specify the SALESFORCE_PASSWORD env var")))(Success(_))

      for {
        username <- usernameTry
        password <- passwordTry
      } yield new ConnectorConfig() {
        setUsername(username)
        setPassword(password)
        setAuthEndpoint(authEndPoint)
      }
    }

    for {
      config <- configTry
      connection <- Try(new PartnerConnection(config))
      instanceUrl <- config.getServiceEndpoint
        .split(servicesEndpointSuffix)
        .headOption
        .fold[Try[String]](Failure(new Error("Could not parse the instance url")))(Success(_))
    } yield connection
  }

  def windowsConnect(username: String, password: String): Try[PartnerConnection] = {
    val servicesEndpointSuffix = "services/Soap/u/40.0/"
    val authEndPoint = "https://login.salesforce.com/" + servicesEndpointSuffix

    val config: ConnectorConfig = {
      new ConnectorConfig() {
        setUsername(username)
        setPassword(password)
        setAuthEndpoint(authEndPoint)
      }
    }

    for {connection<- Try(new PartnerConnection(config))
    } yield connection
  }

}