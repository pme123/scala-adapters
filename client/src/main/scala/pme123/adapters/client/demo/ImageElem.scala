package pme123.adapters.client.demo

import com.thoughtworks.binding.{Binding, dom}
import julienrf.json.derived
import org.scalajs.dom.raw.{Blob, FileReader, HTMLElement, URL}
import org.scalajs.dom.{UIEvent, window}
import play.api.libs.json.{Json, OFormat}
import pme123.adapters.shared.{JobConfig, Logger}
import pme123.adapters.shared.demo.{DemoResult, ImageUpload}
import pme123.adapters.shared.demo.DemoResult._

import scala.scalajs.js
import scala.util.Random

case class ImageElem(demoResult: DemoResult) {

  @dom
  lazy val imageElement: Binding[HTMLElement] = {

      <img style={randomImgStyle} src={ImageElem.urlFromImg(demoResult.img)}/>
  }

  private lazy val randomImgStyle: String = {
    val sHeight = window.screen.height.toInt
    val sWidth = window.screen.width.toInt
    val width = sWidth / 5
    val height = sHeight / 5
    s"""
       | position: absolute;
       | top:${Random.nextInt(sHeight - height) + height / 2}px;
       | left:${Random.nextInt(sWidth - width)}px;
       | max-width:${width}px;
       | max-height:${height}px;
       | font-size:${width}px;
       """.stripMargin
  }

}

object ImageElem extends Logger {
  def urlFromImg(img: Either[ImgUrl, ImgData]): ImgUrl = {

    img match {
      case Left(url) =>
        info(s"Image from URL: $url")
        url
      case Right(data) =>
        info(s"Image from Data: $data")
        val reader = new FileReader()
        reader.readAsText(new Blob(js.Array(data.asInstanceOf[String])), "UTF-8")
        reader.onload = (_: UIEvent) => {
          val imgUrl = URL.createObjectURL(new Blob(js.Array(data.asInstanceOf[String])))
          info(s"Image URL: $imgUrl")
        }
        "todo"
    }
  }
}
