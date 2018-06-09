package pme123.adapters.client.demo

import com.thoughtworks.binding.dom
import play.api.libs.json.{JsResult, JsValue, Json}
import pme123.adapters.client.ToConcreteResults.ConcreteResult
import pme123.adapters.client._
import pme123.adapters.shared.ClientType._
import pme123.adapters.shared._
import pme123.adapters.shared.demo.{DemoJobs, DemoResult}

import scala.language.implicitConversions
import scala.scalajs.js.annotation.JSExportTopLevel

object DemoClient
  extends AdaptersClient {

  // @JSExportTopLevel exposes this function with the defined name in Javascript.
  // this is called by the index.scala.html of the server.
  // the only connection that is not type-safe!
  @JSExportTopLevel("client.DemoClient.main")
  def main(context: String, webPath: String, clientType: String) {
    initClient("DemoClient", context, webPath, clientType)

    ClientType.withNameInsensitiveOption(clientType) match {
      case Some(CUSTOM_PAGE) =>
        DemoView.create()
      case Some(JOB_PROCESS) =>
        val jobDialog: RunJobDialog =
          if (webPath.endsWith(DemoJobs.demoJobIdent))
            DemoRunJobDialog
          else
            DefaultRunJobDialog
        JobProcessView(jobDialog).create()
      case Some(JOB_RESULTS) =>
        JobResultsView(CustomResultsInfos(Seq("Name", "Image Url", "Created")
          ,
          s"""<ul>
                  <li>name, imgUrl: String, * matches any part. Examples are name=Example*, subject=*Excel*</li>
                  <li>$dateTimeAfterL: take created from the defined DateTime, for example: 2017-12-22T12:00</li>
                  <li>$dateTimeBeforeL: take created until the defined DateTime, for example: 2018-01-22T23:00</li>
                </ul>""")
        )(DemoResultForJobResultsRow).create()
      case other => warn(s"Unexpected ClientType: $other")
    }
  }

  implicit object DemoResultForJobResultsRow extends ConcreteResult[JobResultsRow] {

    override def fromJson(lastResult: JsValue): JsResult[JobResultsRow] =
      Json.fromJson[DemoResult](lastResult)
        .map(dr => JobResultsRow(
          Seq(td(dr.name), tdImg(ImageElem.urlFromImg(dr.img)), tdDateTime(dr.created))))
  }


  @dom
  private def tdDateTime(dateTimeStr: String) =
    <td>
      {s"${jsLocalDate(dateTimeStr)}"}<li>
      {jsLocalTime(dateTimeStr)}
    </li>
    </td>
}


