package pme123.adapters.server.entity.demo

import com.typesafe.config.{Config, ConfigFactory}
import pme123.adapters.server.entity.AdaptersContextPropsImplicits
import pme123.adapters.shared.AdaptersContextProp

/**
  * created by pascal.mengelt
  * This config uses the small framework typesafe-config.
  * See here the explanation: https://github.com/typesafehub/config
  * The configuration can be overridden direct in distribution:
  *  - ${DIST_BASE}/conf/demo.conf
  * see here the base: http://stackoverflow.com/questions/6201349/how-to-read-properties-file-placed-outside-war
  *
  * The defaults are described in reference.conf
  */
object DemoAdapterSettings {
  val configPath = "demo.adapters"

  val helloProp = s"hello"
  val numberProp = s"number"
  val passwordProp = s"password"
  val titleShortProp = s"title.short"
  val titleLongProp = s"title.long"

  def config(): Config = ConfigFactory.load()

}

// this settings will be validated on startup
class DemoAdapterSettings(config: Config) {

  import DemoAdapterSettings._

  // checkValid(), just as in the plain SimpleLibContext
  config.checkValid(ConfigFactory.defaultReference(), configPath)
  val projConfig = config.getConfig(configPath)

  val hello: String = projConfig.getString(helloProp)
  val number: Int = projConfig.getInt(numberProp)
  val password: String = projConfig.getString(passwordProp)
  val titleShort: String = projConfig.getString(titleShortProp)
  val titleLong: String = projConfig.getString(titleLongProp)
}

// This is a different way to do DemoAdapterContext, using the
// DemoAdapterSettings class to encapsulate and validate the
// settings on startup
class DemoAdapterContext(val config: Config)
  extends AdaptersContextPropsImplicits {

  import DemoAdapterSettings._

  val name = "demo"

  val settings = new DemoAdapterSettings(config)

  lazy val props: Seq[AdaptersContextProp] = {
    Seq(
      AdaptersContextProp(helloProp, settings.hello)
      , AdaptersContextProp(numberProp, settings.number) // to string by AdaptersContextPropsImplicits
      , AdaptersContextProp(passwordProp, pwd(settings.password)) // make password invisible
      , AdaptersContextProp(titleShortProp, settings.titleShort)
      , AdaptersContextProp(titleLongProp, settings.titleLong)
    )
  }
}

// default Configuration
object DemoAdapterContext extends DemoAdapterContext(DemoAdapterSettings.config())
