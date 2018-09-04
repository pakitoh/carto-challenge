# Carto challenge

## Getting started

Leiningen (https://leiningen.org/) will help us build and test the project.

As we can read in the Leiningen page: 
> Leiningen and Clojure require Java. OpenJDK version 8 is recommended at this time.
> 1. Download the lein script (or on Windows lein.bat)
> 2. Place it on your $PATH where your shell can find it (eg. ~/bin)
> 3. Set it to be executable (chmod a+x ~/bin/lein)
> 4. Run it (lein) and it will download the self-install package

To check if Leiningen is installed we can run in a shell console: *lein version*
```
$> lein version 
Leiningen 2.8.1 on Java 1.8.0_171 OpenJDK 64-Bit Server VM
```

If we want to run the tests: *lein test*
```
$> lein test
 
lein test carto-challenge.activities-test

lein test carto-challenge.data-test

lein test carto-challenge.service-test
INFO  io.pedestal.http  - {:msg "GET /about", :line 80}
INFO  io.pedestal.http  - {:msg "GET /about", :line 80}
INFO  io.pedestal.http  - {:msg "GET /activities", :line 80}
 
lein test carto-challenge.transform-test

Ran 6 tests containing 18 assertions.
0 failures, 0 errors.
```

And if we want to start the application: *lein run*
```
$> lein run
INFO  org.eclipse.jetty.util.log  - Logging initialized @11449ms to org.eclipse.jetty.util.log.Slf4jLog

Creating your server...
INFO  org.eclipse.jetty.server.Server  - jetty-9.4.10.v20180503; built: 2018-05-03T15:56:21.710Z; git: daa59876e6f384329b122929e70a80934569428c; jvm 1.8.0_171-8u171-b11-2-b11
INFO  o.e.j.server.handler.ContextHandler  - Started o.e.j.s.ServletContextHandler@29ab7ccc{/,null,AVAILABLE}
INFO  o.e.jetty.server.AbstractConnector  - Started ServerConnector@13bd25ab{HTTP/1.1,[http/1.1, h2c]}{localhost:8080}
INFO  org.eclipse.jetty.server.Server  - Started @11660ms
 
You can hit Ctr+C to stop the server
```

### Using curl to manually test the system

```
$> curl http://localhost:8080/activities
$> curl http://localhost:8080/activities?excludeCategory=cultura
$> curl http://localhost:8080/activities?excludeCategory=cultura&excludeCategory=nature
$> curl http://localhost:8080/activities?excludeDistrict=Centro
$> curl http://localhost:8080/activities?excludeCategory=cultura&excludeDistrict=Centro
$> curl http://localhost:8080/activities?excludeCategory=cultura&excludeCategory=nature&excludeLocation=outdoors
$> curl http://localhost:8080/recommendations?category=cultural&start=11:00&end=15:00
```
## The challenge

First steps for a bigger project.
Write down notes decribing the development process:
- Explain tradeoffs.
- Describe how do I structure my code.
- Subgoal = Keep an eye on future:
  * More complex rules in the recomendation engine.
  * More cities.
  * More operations in the endpoints.
Make it easy to run and test.

## Step 1: Read the problem carefully and extract information

- Business domain is travels/holidays/vacations.
- The goal is to develop a web API about activities.
- The data we will use is about Madrid. The future will include more cities.

### Requirements

1. Load the data from json file just once.
2. Create GET /activities endpoint:
   - that return all activities in GeoJSON format.
   - that allow to to filter by attrs:
     * location.
     * district.
     * category.
3. Create GET /recommendations endpoint:
   - get as input params:
     * start
     * end
     * category
   - return ONE recommendation that can be visited.
   - choose the longest one if there are more than one available option.
4. Think on future extensibility:
   - do not recommend an outdoors activity on a rainy day.
   - support getting information about activities in multiple cities.
   - extend the recommendation API to fill the given time range with multiple activities.

## Step 2: Initial considerations

### Process

In my career I've had the opportunity to work in a couple of projects following an BDD/ATDD outside-in approach (London style, heavily supported by mocks) and I thought that it was good because in that scenarion the business drives the development and that has a good side. But a not-so-good implementation of the approach leaded us to develop brittle test suites and that's not good. I want to try the *classical TDD* approach this time so I will follow the also called bottom-up TDD style, in which I will try to develop simple components with the identified functionality following the famous red-green-refactor cycle.
I will pile up developed components in order to be able to express complex requirements. Therefore I won't use mocks but for external interactions (that we won't have in this project).
The described domain is quite simple so I won't need to split the code following the DDD precepts as everything is part of the same bounded context in DDD terminology.

### Platform

When choosing a platform we have to take into account a couple of important factors:
- the best tool for the job
- the team that is going to develop 
In the description of the challenge you said that you'd prefer Ruby as it is the platform we will use at Carto. If that's not the case, the more similar to Ruby the better.
I don't know Ruby, I'm a seasoned Java developer that is learning Clojure. So let's try to find out which one is the best option in this scenario:
- Ruby is procedural (imperative?), dinamically typed and object oriented language. It also has automatic memory management and a REPL.
- Java is statically typed, object oriented language. You can describe it as multiparadigm as well as you can write imperative or functional code as well. It has a garbage collector and recently got a REPL altough it's kind of new in the Java world and it's not widely used.
- Clojure is dinamically typed, functional, garbage collected language. It's a LISP style language on top of the JVM and leverage heavily on the REPL.
Both languages run on the JVM hence they have "similar" memory footprint, startup time, related tooling and performance. I know this is a rough statement so please note the "" around the similar word.
I'm way more proficient in Java and in obejct oriented design. My Clojure code is not very idiomatic yet and I still find challenging some functional concepts. On the other hand you're not looking for a Java developer but a software engineer that will adapt to a new environment.
I can't see a clear winner but as I'm the one taking the decision here I will choose *Clojure*. 
I also guess that it will be more exotic to read a solution in a not-so-common language as Java. Clojure will allow me to surface among the huge pile of Java devs. Alea jacta est. :) 

### Data

- The requirement is to read the data file just once.
- The amount of data is tiny so thinking on adding anothere piece of infrastructure to the solution seems overarchitecture it. Even an in-memory database seems too much. 
- Bearing in mind that we want to add more cities in the future, instead of reading directly the file it will be good to configure a data folder and read all the existing files from there.

### Web API

I will create a REST API with 2 endpoints:
- GET /activities
- GET /recommendations
using Pedestal (https://github.com/pedestal/pedestal)

GraphQL could have been an interesting alternative here but as the size of the project is so small, the investment is hardly worth. Lacinia would have been my Clojure library.

## Step 3: Development 

As I decribed before I will try to:
- identify the responsabilities needed to create a solution
- develop a module that will take care of that responsability
- combine them to compose a well tested system.

### Responsabilities

1. Load the data
When the namespace 'carto-challenge.data' is evaluated, the content of all the files in '$PROJECT/data' folder is bound to the *activities* symbol.
This design will follows the open-close principle from SOLID and will allow to add more cities easily in the future.

2. Transform to GeoJSON
Namespace 'carto-challenge.transform' contains *transform* function that will return a valid geoJSON feature of type Point from an activity.The result has been validated using http://geojsonlint.com/

3. Find activities
I started creating a *find-activities* function in 'carto-challenge.activities' namespace that return all activities in memory. Then I added a *filter system* that exclude some of them based on category. Later I updgrade the filter system to allow several exclusion values. Then I had to apply the same to location and district.
When refactored I applied the single responsability principle to split the function into multiple little ones and the open-close principle again. Now it should be easy to add more filter.
I added clojure.java-time (https://github.com/dm3/clojure.java-time) library to help me handle dates.

4. Recomendation engine
The *recomendations* function is based on the *find-activities* one. We just have to add a couple of new filters: include by category and filter based on time (this one is tricky, there are a couple of edge cases I've tried to cover with tests) and some postprocessing (sorting by time spent and taking the first result).
There is one edge case I haven't covered and is related to the kind of time range I'm handling. It is opened in the right end so when you say for example that you want a recommendation at 19:00 and you have just one hour but the activity close at 20:00, the recommendation engine won't give you this one by only 1 second. I think the intention of the API is clear and it should return it that activity but I'm already out of time so I will just comment it the problem in here.

5. Web API
Data, transform and activities are the namespaces that compose the domain layer of the system, the core functionality. We will add a web API layer that will call to the domain. Domain is fully tested and not coupled to the web layer.

### Estimation

It should take a couple of hours. And to be honest, I've spent the whole weekend on this. Why?
- I'm a newbie in Clojure and it's the first time I faced problems that are very common (like loading resources or playing with dates) and that I would have sorted out easily in Java.
- Writing this document is time consuming.
- Understanding GeoJSON structure is another time consuming task that someone that is not familiar with the GIS domain have to do in order to finish the project.

I would like to have added mandatory features in any given projet like input validation or metrics.
Not really sure if I would have spent less time if I've had done it in Java or I'd have added more features.
Anyway this is also useful information. I'm not a quick coder ;)

## Step 4: Extra questions

### Do not recommend an outdoors activity on a rainy day

We should have to add a new rule to the recomendation engine that connect to third party weather system that acts as a different data source.

### Support getting information about activities in multiple cities

Load more data files is trivial now but we shuold have to include the city as input param for the API and change the *activities* synbol by some kind of key-value map with each city data so we can keep the data isolated and a hand at the same time.

### Extend the recommendation API to fill the given time range with multiple activities
We can think of several strategies to fill the time range: 
- recursively: the first activity that fits in is included and then the time range is updated and the function is called again.
- use 'backtracking' or 'dynamic programming' to explore the solutions space and get the best one.

