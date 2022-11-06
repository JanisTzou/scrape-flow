![build status](https://github.com/JanisTzou/scrape-flow/actions/workflows/main.yml/badge.svg)

# ScrapeFlow

## Disclaimer

The library is still under development :-).

It started out recently as an experiment to see if a fluent and declarative approach to web scraping can cover a decent amount of use-cases. 
As a result not much effort has been yet given to proper tests which are added when a part of code seems stable enough.
Usage of the library in other projects is not recommended yet.

## Introduction

ScrapeFlow is a library for asynchronous web scraping. What needs to be scraped is defined as a sequence of steps in a
fluent and declarative way. The resulting code has a tree-like structure that follows the parsed site's DOM structure
and the levels of followed links.

The library aims to solve common scraping problems which can become challenging when using generally available low-level
libraries like HtmlUnit for asynchronous fault-tolerant scraping:

- static sites scraping - internally using HtmlUnit
- dynamic sites scraping - internally using Selenium (*implementation in progress*)
- following links
- pagination
- parallelism
- selective sequential execution of scraping steps
- throttling based on site responsiveness (*to be implemented*)
- retrial of failed requests
- seamless transitioning between static and JS-heavy site scraping
- publishing of scraped data to client code in the order in which it appeared on the scraped sites
- utilities for debugging


## Usage and Sample Code

The parsing is defined as a sequence of steps that are chained fluently and the logic they represent is executed when the scraping is explicitly started.
Each step performs a specific action (e.g. navigating to a page at a given URL) and is followed by other steps (e.g. get all descendant elements 
of the page root, optionally matching some criteria - tags, attributes etc.). When a step finishes it produces a result (e.g. all found descendants)
which are individually passed to the next specified step or steps. 

Apart from DOM traversal operations it is possible to specify at which points the actual site content is to be parsed and how it is to be structured and published 
to client code (collecting data using defined custom "collectors" and publishing it via custom listeners).

For a simple use-case, all that is needed to use the functionality is to create an instance of `Scraping`, and next you can use all the features exposed by the class
 [HtmlUnitFlow](src/main/java/com/github/scrape/flow/scraping/htmlunit/HtmlUnitFlow.java) to define the sequence steps.

For a very simple example see the code below and for more complex scenarios there are some more [demos](src/test/java/com/github/scrape/flow/demos/by/sites).

#### Example static site code snippet to scrape:

```html

<body>
    <!--    ...  -->
    <nav class="regions" role="navigation" aria-label="World">
        <ul>
            <li class="region-section">
                <a class="section-link" href="/news/world/africa">Africa</a>
            </li>
            <li class="region-section">
                <a class="section-link" href="/news/world/asia">Asia</a>
            </li>
            <li class="region-section">
                <a class="section-link" href="/news/world/australia">Australia</a>
            </li>
            <li class="region-section">
                <a class="section-link" href="/news/world/europe">Europe</a>
            </li>
            <li class="region-section">
                <a class="section-link" href="/news/world/latin_america">Latin America</a>
            </li>
        </ul>
    </nav>
    <!--    ...  -->
</body>
```

#### Scraping code example:

To scrape the names of each section from the page sample above and navigate to the detail of each section we can define
the following scraping steps/sequence:

```java

import static com.github.scrape.flow.scraping.htmlunit.HtmlUnit.*;

public class Demo {

 public static void main(String[] args) {

  new Scraping()
          .setSequence(
                  Do.navigateToUrl("https://www.some-news-site.com")
                          .next(Get.descendants().byAttr("aria-label", "World")
                                  .next(Get.descendants().byTag("li")
                                          .addCollector(Section::new, Section.class, new SectionListener())  // for each encountered list item a model is instantiated to hold the scraped data
                                          .next(Get.children().byTag("a")
                                                  .next(Parse.textContent()
                                                          .collectValue(Section::setName, Section.class)  // defines where to put parsed content
                                                  )
                                                  .next(Parse.hRef(href -> "https://www.some-news-site.com" + href)
                                                          .next(goToEachSection())   // impl. omitted
                                                  )
                                          )
                                  )
                          )
          )
          .start(Duration.ofMinutes(2));  // await completion for up to 2 minutes

 }


}


```

```java
// Models to hold the scraped data

public class Section {
    private String name;
    // getters and setters omitted
}
```

```java
// listener for publishing the scraped data

public class SectionListener implements ScrapedDataListener<Section> {
    @Override
    public void onScrapedData(Section data) {
        // do something with parsed data
    }
}
 ```
