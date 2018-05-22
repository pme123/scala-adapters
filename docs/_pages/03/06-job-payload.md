---
title:  "Payload for a Job"
permalink: /docs/03/job-payload/

date:   2018-05-22
categories:
  - development
excerpt: "Provide content for a Job with a web-form."
---
{{page.excerpt}}

The example is based on the`demoJob` from [scala-adapters]. 

By default there is no payload when you start a Job. 
Examples for when you might need a payload:
* Uploading a file as an input source.
* Providing an email address for sending the Job results.
 
## Constraints
* You have created the project with [scala-adapters-g8].

## Create a Dialog
We will create a Dialog to provide the form when running the Job on the root URL `http://localhost:9000`.

![screenshot_job_payload]

We want to add one additional image with its description to our `demoJob` (see `DemoRunJobDialog` for the full code).

Here is the form:
{% highlight scala %}
  private def demoForm: Binding[HTMLElement] = {
    <div class="content">
      <!-- the Semantic-UI form must be initialized after rendering! -->
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
                  // demoDescr, demoImage references the input-ids - a convenience of Binding.scala 
                  // - but sadly not supported by Intellij
                  submitForm(demoDescr, demoImage)
                }>Submit</button>
        <div class="ui error message"></div>
      </form>
    </div>
  }
{% endhighlight %}

Again a nice example of [Binding] working with HTML and [Semantic].

You can use the validation provided by [Semantic]. 
[scala-adapters] just added some classes (`Form`, `Field`, `Rule`) to make it more type-safe.
{% highlight scala %}
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

  private def submitForm(demoDescr: HTMLInputElement, demoImage: HTMLInputElement) {
      if (jQuery(".ui.form").form("is valid").asInstanceOf[Boolean]) {
        val reader = new FileReader()
        reader.readAsDataURL(demoImage.files(0))
        reader.onload = (_: UIEvent) => {
          socket.runAdapter(Some(Json.toJson(ImageUpload(demoDescr.value, s"${reader.result}"))))
        }
    
      }
  }
{% endhighlight %}

We then wrap the payload in a `class` (`ImageUpload`) and send it via the websocket to the server.

## Add the Dialog
Next we need to tell the framework to use this dialog. 
This is done in the routing of the client (`DemoClient`):
{% highlight scala %}
  @JSExportTopLevel("client.DemoClient.main")
  def main(context: String, websocketPath: String, clientType: String): Unit = {
    ClientType.fromString(clientType) match {
      case CUSTOM_PAGE =>
        DemoClient(context, websocketPath).create()
      case JOB_PROCESS =>
        val socket = ClientWebsocket(context)
        JobProcessView(socket, context, websocketPath, DemoRunJobDialog(socket)).create()
      case JOB_RESULTS =>
        ...
      case other => warn(s"Unexpected ClientType: $other")
    }
  }

{% endhighlight %}

Use the dialog (`DemoRunJobDialog`) in the `JobProcessView`. 
The rest is handled by the `scala-adapters`.

## Usage in the Job Process
The payload is available in the JobProcess.
Here is the signature:
{% highlight scala %}
override def runJob(user: String, payload: Option[JsValue])
                      (implicit logService: LogService, jobActor: ActorRef): Future[LogService]
{% endhighlight %}

So all you need to do is extract the payload to the exchange format (`ImageUpload`):
{% highlight scala %}
payload.map(p => Json.fromJson[ImageUpload](p) match {
      case JsSuccess(iu, _) => iu
      case JsError(errors) =>
        val errMsg = s"Problem parsing UploadImage: ${errors.map(e => s"${e._1} -> ${e._2}")}"
        logService.error(errMsg)
        throw JsonParseException(errMsg)
    } 
{% endhighlight %}

{% include web-links.md %}
