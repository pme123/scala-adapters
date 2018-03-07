package pme123.adapters.server.entity

import pme123.adapters.shared.ClientType
import pme123.adapters.server.entity.AdaptersContext.settings.projectConfig

case class ProjectConfig(context: String
                         , projectName: String
                         , clientName: String
                         , clientType: ClientType
                         , websocketPath: String
                         , pageTitle: String
                         , isDemo: Boolean
                         , styleName: Option[String] = None
                        ) {

}

object ProjectConfig {

  def apply(context: String
            , clientType: ClientType
            , websocketPath: String): ProjectConfig =
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
      , if (projectConfig.hasPath("style.name"))
        Some(projectConfig.getString("style.name"))
      else
        None
    )

}
