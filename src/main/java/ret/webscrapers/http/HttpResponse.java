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

package ret.webscrapers.http;

import java.util.Optional;

public class HttpResponse<T> {

    private ResponseType responseType;
    private T data;

    public HttpResponse(ResponseType responseType, T data) {
        this.responseType = responseType;
        this.data = data;
    }

    public HttpResponse(ResponseType responseType) {
        this.responseType = responseType;
    }


    public ResponseType getResponseType() {
        return responseType;
    }

    public Optional<T> getData() {
        return Optional.ofNullable(data);
    }
}
