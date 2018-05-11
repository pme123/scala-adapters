package pme123.adapters.client.demo

import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.UIEvent
import org.scalajs.dom.raw.{Event, FileReader, HTMLElement}
import org.scalajs.jquery.jQuery
import play.api.libs.json.Json
import pme123.adapters.client.SemanticUI.{Field, Form, Rule, jq2semantic}
import pme123.adapters.client.{ClientWebsocket, RunJobDialog, UIStore}
import pme123.adapters.shared.Logger
import pme123.adapters.shared.demo.ImageUpload

import scala.scalajs.js
import scala.scalajs.js.JSON

case class DemoRunJobDialog(socket: ClientWebsocket)
  extends RunJobDialog
    with Logger {

  val form: js.Object = new Form {
    val fields = js.Dynamic.literal(
      demoDescr = new Field {
        val identifier: String = "demoDescr"
        val rules: js.Array[Rule] = js.Array(new Rule {
          val `type`: String = "empty"
        })
      },
      demoImage = new Field {
        val identifier: String = "demoImage"
        val rules: js.Array[Rule] = js.Array(new Rule {
          val `type`: String = "empty"
        })
      }
    )
  }

  @dom
  def create(): Binding[HTMLElement] = {
    val show = UIStore.uiState.showRunJobDialog.bind
    if (show) {
      info("Open DemoRunJobDialog")
      <div class="ui modal detailDialog">
        {//
        demoHeader.bind}{//
        demoForm.bind}
      </div>
    } else
      <div></div>
  }

  // 2. level of abstraction
  // **************************

  @dom
  private def demoHeader: Binding[HTMLElement] = <div class="header">
    Needed Demo Content
  </div>

  @dom
  private def demoForm: Binding[HTMLElement] = {
    <div class="content">
      <iframe style="display:none" onload={_: Event => initForm()}></iframe>
      <form class="ui form">
        <div class="field">
          <label>Description</label>
          <input type="text" id="demoDescr" placeholder="..."/>
        </div>
        <div class="field">
          <input type="file" class="inputFile" id="demoImage" accept="image/*"/>

          <label for="demoImage" class="ui button">
            <i class="ui upload icon"></i>
            Choose image
          </label>
        </div>

        <button class="ui basic icon button"
                onclick={_: Event =>
                  if (jQuery(".ui.form").form("is valid").asInstanceOf[Boolean]) {
                    val reader = new FileReader()
                    reader.readAsDataURL(demoImage.files(0))
                    reader.onload = (_: UIEvent) => {
                      socket.runAdapter(Some(Json.toJson(ImageUpload(demoDescr.value, s"${reader.result}"))))
                    }

                  }

                }>Submit</button>
        <div class="ui error message"></div>
      </form>
    </div>
  }

  // this must be called after rendering!
  private def initForm() = {
    jQuery(".ui.form").form(form)
  }


}
