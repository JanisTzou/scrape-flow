/*
 * Copyright 2021 Janis Tzoumas
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ret.webscrapers.scraping.data.images;


import aaanew.drivers.DriverOperator;
import aaanew.utils.OperationExecutor;
import aaanew.utils.OperationPredicate;
import aaanew.utils.ProcessingException;
import aaanew.utils.SupplierOperation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ret.appcore.model.ScrapedInzerat;
import ret.webscrapers.scraping.data.images.parsers.ImageParser;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ImageScraperImpl implements ImageScraper, DriverOperator {

    private static final Logger log = LogManager.getLogger(ImageScraperImpl.class);
    private final ImageParser imageParser;


    public ImageScraperImpl(ImageParser imageParser) {
        this.imageParser = imageParser;
    }

    @Override
    public List<BufferedImage> scrapeImages(String inzeratUrl, ScrapedInzerat scrapedInzerat) {

        if (scrapedInzerat == null) {
            log.warn("Could not download images, reason: scrapedInzerat == null");
            return Collections.EMPTY_LIST;
        }

        SupplierOperation<Optional<List<BufferedImage>>> scrapeImagesOperation = () -> {
            Optional<List<BufferedImage>> scrapedImagesOp = Optional.empty();
            try {
                List<BufferedImage> bufferedImages = imageParser.scrapeImages(inzeratUrl, scrapedInzerat);
                scrapedImagesOp = Optional.ofNullable(bufferedImages);
                log.debug("Finished scraping images for {} at: {}", scrapedInzerat.getInzeratIdentifier(), inzeratUrl);
            } catch (Exception ex){
                log.error("Error while scraping images for {} at: {}", scrapedInzerat.getInzeratIdentifier(), inzeratUrl, ex);
            }
            return scrapedImagesOp;
        };

        OperationPredicate<Optional<List<BufferedImage>>> dataExistPredicate = (Optional<List<BufferedImage>> dataOp) -> dataOp.isPresent() && dataOp.get().size() > 0;

        Optional<List<BufferedImage>> scrapedImagesOp = Optional.empty();
        try {
            scrapedImagesOp = OperationExecutor.attemptExecute(scrapeImagesOperation, dataExistPredicate, 100, 400, log, "SR: ",
                    Optional.of("FAILED scraping data from url. Keep trying"), "FAILED scraping data from url.", Optional.of("SUCCESS scraping data from url."));
        } catch (ProcessingException e) {
            log.error("Error scraping data from url: ", e);
        }

        imageParser.goToDefaultPage();
        imageParser.restartDriverIfNeeded();

        return scrapedImagesOp.orElse(Collections.EMPTY_LIST);

    }

    @Override
    public boolean terminateDriver() {
        return imageParser.terminateDriver();
    }

    @Override
    public boolean quitDriverIfIdle() {
        return imageParser.quitDriverIfIdle();
    }

    @Override
    public void restartDriverImmediately() {
        imageParser.restartDriverImmediately();
    }

    @Override
    public boolean restartDriverIfNeeded() {
        return imageParser.restartDriverIfNeeded();
    }

    @Override
    public boolean restartOrQuitDriverIfNeeded() {
        return imageParser.restartOrQuitDriverIfNeeded();
    }

    @Override
    public void goToDefaultPage() {
        imageParser.goToDefaultPage();
    }
}
