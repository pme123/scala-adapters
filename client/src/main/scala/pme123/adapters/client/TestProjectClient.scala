package pme123.adapters.client

import scala.language.implicitConversions
import scala.scalajs.js.annotation.JSExportTopLevel

object TestProjectClient {


  @JSExportTopLevel("client.TestProjectClient.main")
  def main(title: String): Unit = {
    AdapterClient("common", title).create()
  }
}