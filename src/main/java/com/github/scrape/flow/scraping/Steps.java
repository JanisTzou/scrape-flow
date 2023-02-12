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

package com.github.scrape.flow.scraping;

import java.util.ArrayList;
import java.util.List;

public class Steps {

    // TODO make non static
    private static final List<Step> roots = new ArrayList<>();

    public static void add(int number, int parent) {
        // TODO if number already has a parent then find the root parent and attach it to the specified parent ...
        Step existingStep = findStep(number);
        if (existingStep != null) {
            Step rootParent = existingStep.findRootParent();
            if (rootParent != null) {
                Step existingRootParent = findStep(rootParent.number); // TODO do we need this here ?
                if (existingRootParent != null) {
                    existingRootParent.addChild(existingStep);
                    existingStep.setParent(existingRootParent);
                    Step prevParent = existingStep.parent;
                    if (prevParent != null) {
                        prevParent.removeChild(existingStep);
                    }
                }
            }
        } else {
            if (roots.isEmpty()) {
                Step p = new Step(parent);
                Step s = new Step(number, p);
                p.addChild(s);
                roots.add(p);
            } else {
                Step detachedParent = roots.stream().filter(r -> r.number == number).findFirst().orElse(null); // TODO this needs to be removed from roots ...
                if (detachedParent == null) {
                    Step p = findStep(parent);
                    if (p == null) {
                        Step p0 = new Step(parent);
                        Step s = new Step(number, p0);
                        p0.addChild(s);
                        roots.add(p0);
                    } else {
                        Step s = new Step(number, p);
                        p.addChild(s);
                    }
                } else {
                    Step p = findStep(parent);
                    if (p == null) {
                        Step p0 = new Step(parent);
                        p0.addChild(detachedParent);
                        roots.add(p0);
                    } else {
                        p.addChild(detachedParent);
                    }
                    roots.remove(detachedParent);
                }
            }
        }

    }

//    static Step findParent(int parent) {
//        for (Step step : roots) {
//            Step p = findParent(step, parent);
//            if (p != null) {
//                return p;
//            }
//        }
//        return null;
//    }
//
//    static Step findParent(Step step, int parent) {
//        if (step.number == parent) {
//            return step;
//        } else {
//            for (Step child : step.children) {
//                return findParent(child, parent);
//            }
//        }
//        return null;
//    }

    static Step findStep(int number) {
        for (Step step : roots) {
            Step s = findStep(step, number);
            if (s != null) {
                return s;
            }
        }
        return null;
    }

    static Step findStep(Step step, int number) {
        if (step.number == number) {
            return step;
        } else {
            for (Step child : step.children) {
                return findStep(child, number);
            }
        }
        return null;
    }



    public static class Step {
        private final int number;
        private Step parent;
        private final List<Step> children = new ArrayList<>();

        public Step(int number) {
            this.number = number;
        }

        public Step(int number, Step parent) {
            this.number = number;
            this.parent = parent;
        }

        public void addChild(Step child) {
            this.children.add(child);
        }

        public void removeChild(Step child) {
            this.children.remove(child);
        }

        public void setParent(Step parent) {
            this.parent = parent;
        }

        public Step find(int number) {
            if (this.number == number) {
                return this;
            } else {
                for (Step child : children) {
                    Step found = child.find(number);
                    if (found != null) {
                        return found;
                    }
                }
            }
            return null;
        }

        public Step findRootParent() {
            if (this.parent == null) {
                return null;
            } else {
                return this.parent.findRootParent();
            }
        }

    }

}
