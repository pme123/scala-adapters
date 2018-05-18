package pme123.adapters.server.entity

import pme123.adapters.shared.ClientType
import pme123.adapters.server.entity.AdaptersContext.settings.projectConfig

case class ProjectConfig(context: String // the context the project is running (play.http.context)
                         , projectName: String // the name of the project (Settings.projectName of your project)
                         , clientName: String // the exported name of the client Entry method (see for example pme123.adapters.client.demo.DemoClient.main)
                         , clientType: ClientType // type of the page
                         , websocketPath: String // the path of the websocket to use (can also be used to differentiate between JobProcesses)
                         , pageTitle: String // title displayed on the webpage
                         , isDemo: Boolean // demo is within scala-adapters - and so the paths are different
                         , isDevMode: Boolean // in dev mode you have fast opt JavaScripts
                         , styleName: Option[String] = None // name of a custom CSS-file
                        ) {

}

object ProjectConfig {

  def apply(context: String
            , clientType: ClientType
            , websocketPath: String
            , isDevMode: Boolean
           ): ProjectConfig =
    new ProjectConfig(context
      , projectConfig.getString("name")
      , projectConfig.getString("client.name")
      , clientType
      , websocketPath
      , projectConfig.getString("page.title")
      , if (projectConfig.hasPath("demo"))
        projectConfig.getBoolean("demo")
      else
        false
      , isDevMode
      , if (projectConfig.hasPath("style.name"))
        Some(projectConfig.getString("style.name"))
      else
        None
    )

}
