---
title:  "Server Jobs"
permalink: /docs/03/server-jobs/

date:   2015-11-19
categories:
  - development
excerpt: "Create static and dynamic Server Jobs."
---
{{page.excerpt}}

## Constraints
You have created the project with [scala-adapters-g8]

## Job Configuration
The jobs are configured in the `reference.conf`, here from the `scala-adapters` project.
{% highlight json %}
```
  job.configs = [{
    ident = "demoJob"
    schedule {
      // the first time of day the Import should run (this is the Server time!). (format is HH:mm)
      // Default is 01:00
      first.time = "03:00"
      // the first weekday if needed (e.g. to emulate once a week)
      // possible: "monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday" or "-" for nothing
      first.weekday = "tuesday"

      // the period the Adapter should call the Coop Webservice
      // Default is one day (1440 minutes) - be aware 1 minute is the smallest period possible
      // - and it must be greater than the time the import takes!
      // make also sure that the period is so that the import is at always the same time of day.
      interval.minutes = 2
    }
  }, {
    ident = "demoJobWithDefaultScheduler"
    schedule {
    }
  }, {
    ident = "demoJobWithoutScheduler"
  }]
```
{% endhighlight %}

You see above the possible configurations (with- and without a Scheduler defined).
Each Job needs a unique ident!

## Job Creation
You have to provide a Factory that creates the JobProcess vor a JobConfig.

### Static Creation
Static creation means that the Jobs are created at Startup in the trait `JobCreation`.
All you need to do shows the example from `scala-adapters`

{% highlight scala %}
```
// the Factory must be a Singleton
@Singleton
class DemoJobCreation @Inject()(demoJob: DemoJobProcess // each JobProcess is injected
                                , demoJobWithDefaultScheduler: DemoJobWithDefaultSchedulerActor
                                , demoJobWithoutScheduler: DemoJobWithoutSchedulerActor
                                , @Named("actorSchedulers") val actorSchedulers: ActorRef // needed to create Schedules
                                , actorSystem: ActorSystem // needed to create the JobActors
                              )(implicit val ec: ExecutionContext) // needed for async processing
  extends JobCreation {

  // create all JobActors
  private lazy val demoJobRef = actorSystem.actorOf(JobActor.props(jobConfigs(demoJobIdent), demoJob), demoJobIdent)
  private lazy val demoJobWithDefaultSchedulerRef = actorSystem.actorOf(JobActor.props(jobConfigs(demoJobWithDefaultSchedulerIdent), demoJobWithDefaultScheduler), demoJobWithDefaultSchedulerIdent)
  private lazy val demoJobWithoutSchedulerRef = actorSystem.actorOf(JobActor.props(jobConfigs(demoJobWithoutSchedulerIdent), demoJobWithoutScheduler), demoJobWithoutSchedulerIdent)

  // return the correct JobActor for a JobConfig
  def createJobActor(jobConfig: JobConfig): ActorRef = jobConfig.jobIdent match {
    case "demoJob" => demoJobRef
    case "demoJobWithDefaultScheduler" => demoJobWithDefaultSchedulerRef
    case "demoJobWithoutScheduler" => demoJobWithoutSchedulerRef
    case other => throw ServiceException(s"There is no Job for $other")
  }
}

``` 
{% endhighlight %}

### Dynamic Creation
Dynamic creation means that the Jobs are created at Runtime in your _Factory_.
The example is from a Calendar integration, where different clients use different Service-URLs.
 (only differences to static creation are explained)

{% highlight scala %}
```
@Singleton
class CalendarJobCreation @Inject()(calendarImporter: CalendarImporter // the import infrastructure
                                    , calendarService: CalendarService // the service to integrate
                                    , @Named("actorSchedulers") val actorSchedulers: ActorRef
                                    , actorSystem: ActorSystem
                                   )(implicit val mat: Materializer
                                     , val ec: ExecutionContext)
  extends JobCreation {

  def createJobActor(jobConfig: JobConfig): ActorRef = create(jobConfig)

  private def create(jobConfig: JobConfig): ActorRef = {
    // creates a JobProcess from the JobConfig, that includes a subWebPath sent by the client.
    val process = CalendarProcess(jobConfig, calendarImporter, calendarService)
    val jobActor = actorSystem.actorOf(JobActor.props(jobConfig, process))
    initSchedule(jobConfig, jobActor)
    jobActor
  }

  // return empty Map onStartUp - as the JobProcesses are created on the fly
  override def createJobActorsOnStartUp(): Map[JobConfig, ActorRef] = Map()

}
``` 
{% endhighlight %}

## Job Process
Implements the Job logic itself, like:
* Server Batch Jobs
* Handling Client Requests
* Implement Chat-Bot
* etc.

Here the implementation you have in your [get-started] Project.

{% highlight scala %}

```
class GetStartedProcess @Inject()()
                             (implicit val mat: Materializer, val ec: ExecutionContext)
  extends JobProcess {

  val jobLabel = "GetStarted Job"

  def createInfo(): ProjectInfo = // check createInfo for adding more infos!
    createInfo(version.BuildInfo.version)

  // the process fakes some long taking tasks that logs its progress
  def runJob(user: String)
            (implicit logService: LogService
             , jobActor: ActorRef): Future[LogService] = {
    Future {
      logService.startLogging()
      val results = ... // e.g. call a service

      results.foreach(doSomeWork)
      logService // fluent api
    }
  }

  protected def doSomeWork(dr: GetStartedResult)
                          (implicit logService: LogService): LogEntry = {
    ...
    logService.log(ll, s"Job: $jobLabel $ll: ${dr.name}", detail)
  }
} 
```
{% endhighlight %}

## Register Factory
Now you have to tell Guice (Dependency Injection) what class is responsible for the Job creation.

In your [get-started] Project this already done in `Module`.

{% highlight scala %}

```
class Module extends AbstractModule with AkkaGuiceSupport {

  def configure(): Unit = {
    bind(classOf[JobCreation])
      .to(classOf[GetStartedJobCreation])
      .asEagerSingleton() // make it eager - as the Job is static and created at startup

    ... // more configuration
  }
}
```
{% endhighlight %}

## Check the Result
Run the Project, as described here [get-started::run]

If you have **only one Job**, then [http://localhost:9000](http://localhost:9000) is all you need.

If you have **more than one Job**, you use `http://localhost:9000/jobProcess/JOB_NAME`.

By default it takes the first Job of the configuration.

{% include web-links.md %}
