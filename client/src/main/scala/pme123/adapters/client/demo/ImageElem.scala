package pme123.adapters.client.demo

import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.raw.HTMLElement
import org.scalajs.dom.window
import pme123.adapters.shared.demo.DemoResult

import scala.util.Random

case class ImageElem(demoResult: DemoResult) {

  @dom
  lazy val imageElement: Binding[HTMLElement] =
      <img style={randomImgStyle} src={demoResult.imgUrl}/>

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