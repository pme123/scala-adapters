package pme123.adapters.server.control

import org.scalatestplus.play.ConfiguredApp

import scala.reflect.ClassTag

/**
  * Created by pascal.mengelt on 20.10.2016.
  */
trait GuiceAcceptanceSpec
  extends AcceptanceSpec
    // if you want to test only one Test you need:
    // with GuiceOneServerPerSuite {
    // if you want to test all:
    with ConfiguredApp {

  def inject[A](implicit tag: ClassTag[A]): A =
    app.injector.instanceOf(tag)
}
