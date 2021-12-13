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

package ret.webscrapers.scraping.data.inzeraty;


import aaanew.utils.OperationExecutor;
import aaanew.utils.OperationPredicate;
import aaanew.utils.ProcessingException;
import aaanew.utils.SupplierOperation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ret.appcore.model.Inzerat;
import ret.appcore.model.ScrapedInzerat;
import ret.appcore.model.WebSegment;
import ret.appcore.model.enums.TypNabidkyEnum;
import ret.appcore.model.enums.TypNemovitostiEnum;
import ret.appcore.model.enums.WebsiteEnum;
import ret.webscrapers.scraping.data.inzeraty.parsers.InzeratParser;

import java.util.Optional;

public class InzeratScraperImpl implements InzeratScraper {

	private static final Logger log = LogManager.getLogger(InzeratScraperImpl.class);
	private final InzeratParser inzeratParser;


	public InzeratScraperImpl(InzeratParser inzeratParser) {
		this.inzeratParser = inzeratParser;
	}


	@Override
	public Optional<ScrapedInzerat> scrapeInzerat(WebSegment webSegment, String inzeratUrl) {

		WebsiteEnum website = webSegment.getWebsite();
		TypNabidkyEnum typInzeratu = webSegment.getTypNabidky();
		TypNemovitostiEnum typNemovitosti = webSegment.getTypNemovitosti();
		String inzeratIdentifier = Inzerat.makeIdentifier(inzeratUrl);

		SupplierOperation<Optional<ScrapedInzerat>> scrapeInzeratOperation = () -> {
			Optional<ScrapedInzerat> scrapedInzeratOp = Optional.empty();
			try {
				scrapedInzeratOp = inzeratParser.scrapeData(inzeratUrl, website, typInzeratu, typNemovitosti, webSegment);
				log.debug("Finished scraping inzerat for {} at: {}", inzeratIdentifier, inzeratUrl);
			} catch (Exception ex){
				log.error("Error inzerat for {} at: {}", inzeratIdentifier, inzeratUrl, ex);
			}
			return scrapedInzeratOp;
		};

		OperationPredicate<Optional<ScrapedInzerat>> dataExistPredicate = Optional::isPresent;

		Optional<ScrapedInzerat> scrapedInzeratOp = Optional.empty();
		try {
			scrapedInzeratOp = OperationExecutor.attemptExecute(scrapeInzeratOperation, dataExistPredicate, 100, 400, log, "",
					Optional.of("FAILED scraping data from url. Keep trying"), "FAILED scraping data from url.", Optional.of("SUCCESS scraping data from url."));
		} catch (ProcessingException e) {
			log.error("Error scraping data for {} at url: {}", Inzerat.makeIdentifier(inzeratUrl), inzeratUrl, e);
		}

		inzeratParser.goToDefaultPage();
		inzeratParser.restartOrQuitDriverIfNeeded();

		return scrapedInzeratOp;
	}

	@Override
	public boolean terminateDriver() {
		return inzeratParser.terminateDriver();
	}

	@Override
	public boolean quitDriverIfIdle() {
		return inzeratParser.quitDriverIfIdle();
	}

	@Override
	public void restartDriverImmediately() {
		inzeratParser.restartDriverImmediately();
	}

	@Override
	public boolean restartDriverIfNeeded() {
		return inzeratParser.restartDriverIfNeeded();
	}

	@Override
	public boolean restartOrQuitDriverIfNeeded() {
		return inzeratParser.restartOrQuitDriverIfNeeded();
	}

	@Override
	public void goToDefaultPage() {
		inzeratParser.goToDefaultPage();
	}

}
