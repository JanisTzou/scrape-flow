# Scrape Flow

Note: The library is still under development

## Introduction

Experimental library for asynchronous web scraping. What needs to be scraped is defined as a sequence of steps in a fluent and declarative way. 
The resuling code has a tree-like structure that follows the parsed site's DOM structure and the levels of following links.

The library aims at solving common scraping problems which can become challenging when using generally available low-level libraries like HtmlUnit for asynchronous fault-tolerant scraping:

- following links
- pagination
- parallel scraping
- selective sequential execution of scraping steps
- throttling based on site reponsiveness (To be implemented)
- retrial of failed requests
- transitioning between static ad JS-powered site scraping (To be implemented)
- publishing of scraped data to clients in the order in which it appeared on the web
- utilities for debugging

## Usage

To be added when project is more ready :-)

## Sample code

#### Example static site code snipped to scrape:

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


#### Scraping setup:

```java

    // some bootstrapping omitted ...

    // When we start scraping, we will receive the loaded starting page - the sequence below
    // can just define the DOM traversal with some other actions (parsing, navigation, data collection and publishing and more)

    scraping.setSequence(
        Get.descendants().byAttr("aria-label", "World")
            .next(Get.children().byTag("li")
                .addCollector(Section::new, Section.class, new ScrapedSectionListener())  // for each encountered list item a model is instantiated to hold the scraped data
                .next(Get.descendants().byTag("a")
                    .next(Parse.textContent()
                        .collectOne(Section::setName, Section.class)  // defines where to put parsed content
                    )
                    .next(Parse.hRef(href -> "https://www.some-news-site.com" + href)
                        .nextNavigate(goToEachSection())
                    )
                )
            )
    );


    // actually start scraping
    scraper.start(scraping, "https://www.some-news-site.com/world");
    scraper.awaitCompletion(Duration.ofMinutes(1));
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
    
    public class ScrapedSectionListener implements ScrapedDataListener<Section> {
        @Override
        public void onParsedData(Section data) {
            // do something with parsed data
        }
    }
 ```
