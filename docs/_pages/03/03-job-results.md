---
title:  "Job Results"
permalink: /docs/03/job-results/

date:   2018-03-20
categories:
  - development
excerpt: "Create and use Job Results."
---
{{page.excerpt}}

The example is based on `DemoResultClient` from [scala-adapters].

## Constraints
* You have created the project with [scala-adapters-g8].
* You have your Job ready ([server-jobs]). 
* The Job must create some result (like data from a webservice request).

## Shared Data
One of the big advantages of having Scala on the server and the client is that you have to define the data you exchange only once!

So lets create `/shared/src/main/scala/shared/GetStartedResult`

{% highlight scala %}
case class GetStartedResult(name: String, imgUrl: String, created: DateTimeString)

object GetStartedResult {
  implicit val jsonFormat: OFormat[GetStartedResult] = Json.format[GetStartedResult]
}
{% endhighlight %}

In essence this needs 2 lines of code:
1. Define the Data as a Case Class (this could also be a complex structure of case classes).
1. Provide a Json -marshaller/ -un-marshaller.

## Client Configuration
The client you use is defined by configuration (see `reference.conf` of [scala-adapters])

In your [get-started] Project you have:
{% highlight json %}
  project.config {
    name = "get-started"
    client.name = "DefaultClient"
    page.title = "Get Started Adapter"
  }
{% endhighlight %}

We only have to replace `"DefaultClient"` to `"GetStartedClient"` 

## Create Client
[scala-adapters] provides a default implementation (`client.name = "DefaultClient"`)

{% highlight scala %}
object DefaultClient {

  LoggerConfig.factory = ConsoleLoggerFactory()

  // @JSExportTopLevel exposes this function with the defined name in Javascript.
  // this is called by the index.scala.html of the server.
  // the only connection that is not type-safe!
  @JSExportTopLevel("client.DefaultClient.main") 
  def mainPage(context: String
                   , websocketPath: String
                   , clientType: String): Unit = {
    ClientType.fromString(clientType) match {
      case JOB_PROCESS =>
        JobProcessView(context, websocketPath).create()
      case JOB_RESULTS =>
        DefaultView("there is no JobResults page defined").create()
      case CUSTOM_PAGE =>
        DefaultView("there is no custom page defined").create()
      case other => warn(s"Unexpected ClientType: $other")
    }

  }
}
{% endhighlight %}

Lets create the Client from that: `/client/src/main/scala/client/GetStartedClient.scala`

We create an implementation for the `case JOB_RESULTS =>`:

{% highlight scala %}
object GetStartedClient
  extends ClientImplicits // explanations below
    with Logger {
   ...
  // "client.CLIENT_NAME.main" - where CLIENT_NAME must match client.name from reference.conf
  @JSExportTopLevel("client.GetStartedClient.main")
  def main(context: String, websocketPath: String, clientType: String): Unit = {
    ...
      case JOB_RESULTS =>
        JobResultsView(context
          , websocketPath
          , CustomResultsInfos(Seq("Name", "Image Url", "Created")
            ,
            s"""<ul>
                  <li>name, imgUrl: String, * matches any part. Examples are name=Example*, subject=*Excel*</li>
                  <li>$dateTimeAfterL: take created from the defined DateTime, for example: 2017-12-22T12:00</li>
                  <li>$dateTimeBeforeL: take created until the defined DateTime, for example: 2018-01-22T23:00</li>
                </ul>""")
        )(GetStartedResultForJobResultsRow).create() // even it's implicit - GetStartedResultForJobResultsRow is needed here
      ...
    }
  }

}
{% endhighlight %}

The Implementation has 2 new elements:
1. `CustomResultsInfos` that defines the header of the result table and a Tooltip for the Result Filter.
1. `GetStartedResultForJobResultsRow` is the Type Class instance for GetStartedResult that is used to create a JobResultsRow.

Here is its implementation:
{% highlight scala %}
  private implicit object GetStartedResultForJobResultsRow extends ConcreteResult[JobResultsRow] {

    override def fromJson(lastResult: JsValue): JsResult[JobResultsRow] =
      Json.fromJson[GetStartedResult](lastResult)
        .map(dr => JobResultsRow(
          Seq(td(dr.name), tdImg(dr.imgUrl), tdDateTime(dr.created))))
  }


  @dom
  private def tdDateTime(dateTimeStr: String) =
    <td>
      {s"${jsLocalDate(dateTimeStr)}"}
      <li>{jsLocalTime(dateTimeStr)}</li>
    </td>
{% endhighlight %}

It creates a row of the Result Table, using predefined elements provided by a Utility.

`tdDateTime` is an example on how to customize a table cell.

## Adjust the JobProcess
We need 2 things:
First: A Result object that handles filtering and sorting:

{% highlight scala %}
case class GetStartedResults(results: Seq[GetStartedResult])
  extends AConcreteResult
    with Logger {

  // type class instance for GetStartedResult
  implicit object filterableGetStartedResult extends Filterable[GetStartedResult] {
    
    // column to sort results
    def sortBy(filterable: GetStartedResult): String = filterable.name

    // filters the results
    def doFilter(filterable: GetStartedResult)(implicit filters: Map[String, String]): Boolean = {
      matchText("name", filterable.name) &&
        matchText("imgUrl", filterable.imgUrl) &&
        matchDate(dateTimeAfterL, filterable.created, (a, b) => a <= b) &&
        matchDate(dateTimeBeforeL, filterable.created, (a, b) => a >= b)
    }
  }

  // method to filter the results for a ClientConfig
  def clientFiltered(clientConfig: ClientConfig): Seq[JsValue] =
    ClientConfig.filterResults(results, clientConfig)
      .map(dr => Json.toJson(dr))

  // merge the results with an existing one
  def merge(other: AConcreteResult): AConcreteResult = other match {
    case GetStartedResults(toMerge) =>
      GetStartedResults(results ++ toMerge)
    case unexpected =>
      warn(s"Not expected message: $unexpected")
      this
  }
}
{% endhighlight %}

Second: A small change in the JobProcess:
{% highlight scala %}
import akka.pattern.ask
...
      jobActor ? LastResult(GetStartedResults(Nil)) // reset last result
      // replace results.foreach(doSomeWork) with:
      results.foreach { dr =>
        doSomeWork(dr)
        jobActor ? LastResult(GetStartedResults(Seq(dr)), append = true)
      }
...
{% endhighlight %}

## Check Result
Running the Project and **running the Job**, should give you this:

![screenshot_job_results]

Check your implementation:
* tooltip of the Filter Results..
* header of table
* row of table (the list element of the time)

{% include web-links.md %}
