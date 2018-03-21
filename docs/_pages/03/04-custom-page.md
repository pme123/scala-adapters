---
title:  "Custom Page"
permalink: /docs/03/custom-page/

date:   2018-03-20
categories:
  - development
excerpt: "Create and use a Custom Page."
---
{{page.excerpt}}

The example is based on `DemoResultClient` from [scala-adapters]. 
The Custom Page displays the incoming images at random positions and in random sizes.

## Constraints
* You have created the project with [scala-adapters-g8].
* You have your Job Results ready ([job-results]).
* A basic understanding of Type Classes in Scala (see [scala-type-classes]). 

## Shared Data
We can reuse `/shared/src/main/scala/shared/GetStartedResult`

## Create HtmlElement
First we create the HTMLElement with for the image `/client/src/main/scala/client/GetStartedClient.scala`
{% highlight scala %}
case class ImageElem(getStartedResult: GetStartedResult) {

  @dom
  lazy val imageElement: Binding[HTMLElement] =
      <img style={randomImgStyle} src={getStartedResult.imgUrl}/>
  ...
}
{% endhighlight %}

`randomImgStyle` creates the random position and in random size of the image.

## Extend the UIStore
We extend the UIStore with `UIGetStartedStore`
{% highlight scala %}
trait GetStartedUIStore
  extends UIStore {

  protected def getStartedUIState: GetStartedUIState

  // type class instance for ImageElem
  implicit object concreteResultForImageElem extends ConcreteResult[ImageElem] {

    override def fromJson(lastResult: JsValue): JsResult[ImageElem] =
      Json.fromJson[GetStartedResult](lastResult)
        .map(ImageElem)
  }

  // provide a function to update the ImageElems from the ConcreteResults
  def updateImageElems(lastResults: Seq[JsValue]): Seq[ImageElem] = {
    ToConcreteResults.toConcreteResults(getStartedUIState.imageElems, lastResults)
  }
}
{% endhighlight %}

The idea of a UIStore is, that **changing the UIState is done just in one place**.

## Create the Client

{% highlight scala %}
case class GetStartedClient(context: String, websocketPath: String)
  extends AdaptersClient
    with GetStartedUIStore {

  // init the custom UIState
  val getStartedUIState = GetStartedUIState()

  // create the websocket
  private lazy val socket = ClientWebsocket(uiState, context)

  @dom
  protected def render: Binding[HTMLElement] = {
    socket.connectWS(Some(websocketPath))
    <div>
      {imageContainer.bind}
    </div>
  }

  @dom
  private def imageContainer = {
    val lastResults = uiState.lastResults.bind
    val imageElems = updateImageElems(lastResults)
    <div>
      {Constants(imageElems: _*)
      .map(_.imageElement.bind)}
    </div>
  }
}
{% endhighlight %}

This is more or less a composition of HTMLElements, done with [Binding.scala][Binding].

Here is a small intro to Binding.scala [binding-google-maps].

## Update Client Application
We have to update our client slightly `/client/src/main/scala/client/GetStartedClient.scala`

{% highlight scala %}
      ...
      case CUSTOM_PAGE =>
        GetStartedClient(context, websocketPath).create()
      ...
{% endhighlight %}

As we have now our implementation for our `CUSTOM_PAGE`.

## Check Result
Running the Project and **running the Job**, should give you this:

![screenshot_custom_page]


{% include web-links.md %}
