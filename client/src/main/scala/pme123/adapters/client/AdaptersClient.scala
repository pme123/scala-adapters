package pme123.adapters.client

import pme123.adapters.shared.Logger
import slogging.{ConsoleLoggerFactory, LoggerConfig}

trait AdaptersClient
  extends ClientUtils
    with Logger {

  LoggerConfig.factory = ConsoleLoggerFactory()

  def initClient(clientName: String
                 , context: String
                 , webPath: String
                 , clientType: String): Unit = {
    info(s"$clientName $clientType: $context$webPath")
    UIStore.changeWebContext(context)
    UIStore.changeWebPath(webPath)
    ClientWebsocket.connectWS()
  }
}
