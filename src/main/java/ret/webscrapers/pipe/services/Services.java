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

package ret.webscrapers.pipe.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ret.webscrapers.cycles.CyclesService;
import ret.webscrapers.scraping.data.images.query.ImageQueryService;
import ret.webscrapers.scraping.data.inzeraty.query.InzeratQueryService;
import ret.webscrapers.websegments.WebSegmentsService;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Services {

    private static Logger log = LogManager.getLogger(Services.class);

    private final CyclesService cyclesService;
    private final WebSegmentsService webSegmentsService;
    private final InzeratQueryService inzeratQueryService;
    private final ImageQueryService imageQueryService;
    private final List<PipeService> serviceList;


    public Services(CyclesService cyclesService,
                    WebSegmentsService webSegmentsService,
                    InzeratQueryService inzeratQueryService,
                    ImageQueryService imageQueryService) {

        this.cyclesService = cyclesService;
        this.webSegmentsService = webSegmentsService;
        this.inzeratQueryService = inzeratQueryService;
        this.imageQueryService = imageQueryService;

        this.serviceList = new CopyOnWriteArrayList<>();
        this.serviceList.add(cyclesService);
        this.serviceList.add(webSegmentsService);
        this.serviceList.add(inzeratQueryService);
        this.serviceList.add(imageQueryService);
    }


    public enum Operation {
        INITIALISATION,
        UPDATE
    }


    public boolean initOrUpdateServices(Operation operation) {
        boolean overallSuccess = true;
        for (PipeService service : serviceList) {
            boolean success = service.initialiseOrUpdate();
            String className = service.getClass().getSimpleName();
            if (!success) {
                overallSuccess = false;
                log.info(">>> {} of service '{}' FAILED <<<", operation, className);
            } else {
                log.info(">>> {} of service '{}' SUCCESSFUL <<<", operation, className);
            }
        }

        return overallSuccess;
    }

    public CyclesService getCyclesService() {
        return cyclesService;
    }

    public WebSegmentsService getWebSegmentsService() {
        return webSegmentsService;
    }

    public InzeratQueryService getInzeratQueryService() {
        return inzeratQueryService;
    }

    public ImageQueryService getImageQueryService() {
        return imageQueryService;
    }
}
