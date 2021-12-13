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

package ret.webscrapers.data.send.handlers;


import ret.appcore.model.wrappers.JsonWrapper;

public class SendResult {

    private final boolean sentSuccess;
    private final JsonWrapper data;

    public SendResult(boolean sentSuccess, JsonWrapper data) {
        this.sentSuccess = sentSuccess;
        this.data = data;
    }

    public boolean isSuccessful() {
        return sentSuccess;
    }

    public JsonWrapper getData() {
        return data;
    }
}
