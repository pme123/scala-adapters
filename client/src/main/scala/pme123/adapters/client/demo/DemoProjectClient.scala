package pme123.adapters.client.demo

import pme123.adapters.client.AdapterClient

import scala.language.implicitConversions
import scala.scalajs.js.annotation.JSExportTopLevel

object DemoProjectClient {

  @JSExportTopLevel("client.DemoProjectClient.main")
  def main(title: String): Unit = {
    AdapterClient("adapters", title).create()
  }
}