package pme123.adapters.client.demo

import pme123.adapters.client.JobCockpitClient

import scala.language.implicitConversions
import scala.scalajs.js.annotation.JSExportTopLevel

object DemoProjectClient {

  @JSExportTopLevel("client.DemoProjectClient.main")
  def main(title: String): Unit = {
    JobCockpitClient("adapters", title).create()
  }
}