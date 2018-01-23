package pme123.adapters.shared

sealed trait ConfigValue

case class ConfigInt(value: Int) extends ConfigValue

case class ConfigString(value: String) extends ConfigValue

case class ConfigList(value: List[ConfigValue]) extends ConfigValue

case class ConfigObject(value: Map[String, ConfigValue]) extends ConfigValue