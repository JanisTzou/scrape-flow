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

package com.github.scrape.flow.scraping.selenium;

import com.github.scrape.flow.clients.ClientReservationType;
import com.github.scrape.flow.execution.StepOrder;
import com.github.scrape.flow.scraping.ScrapingContext;
import com.github.scrape.flow.scraping.ScrapingServices;
import org.apache.commons.lang3.NotImplementedException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SeleniumGetAncestor extends SeleniumScrapingStep<SeleniumGetAncestor>
    // TODO these filters do not ake sense for the parent as they are .... if we want to provide them, the implementation must go throguh all parents until it finds the one specified by the filters ...
    //  if ued as they are the filters might just filter away the one parent found ...
//        implements
//        FilterableByAttribute<GetParent>,
//        FilterableByTag<GetParent>,
//        FilterableByTextContent<GetParent>,
//        FilterableByCssClass<GetParent>
{

    private final Type type;
    private final Integer param;

    SeleniumGetAncestor(Type type, @Nullable Integer param) {
        this.type = type;
        this.param = param;
    }

    SeleniumGetAncestor(Type type) {
        this(type, null);
    }

    @Override
    protected SeleniumGetAncestor copy() {
        return copyFieldValuesTo(new SeleniumGetAncestor(type, param));
    }

    @Override
    protected StepOrder execute(ScrapingContext ctx, ScrapingServices services) {
        StepOrder stepOrder = services.getStepOrderGenerator().genNextAfter(ctx.getPrevStepOrder());

        Runnable runnable = () -> {
            WebElement we = ctx.getWebElement();
            Supplier<List<WebElement>> search = () -> {
                if (type.equals(Type.PARENT)) {
                    return Stream.of(we.findElement(By.xpath("./.."))).collect(Collectors.toList());
                } else if (type.equals(Type.NTH_ANCESTOR)) {
//                    return SeleniumUtils.findNthParent(we, param).stream().collect(Collectors.toList());
                    throw new NotImplementedException("not implemented");
                }
                return Collections.emptyList();
            };
            getHelper().execute(ctx, search, stepOrder, getExecuteIf(), services);
        };

        submitForExecution(stepOrder, runnable, services.getTaskService());

        return stepOrder;
    }

    @Override
    protected ClientReservationType getClientReservationType() {
        return ClientReservationType.READING;
    }


    public enum Type {
        PARENT,
        NTH_ANCESTOR,
    }

    // TODO see comment above ... about filters ...
//    @Override
//    public GetParent addFilter(Filter filter) {
//        return super.addFilter(filter);
//    }
}
