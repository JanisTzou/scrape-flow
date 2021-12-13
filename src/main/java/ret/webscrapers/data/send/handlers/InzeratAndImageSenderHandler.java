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


import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ret.appcore.model.wrappers.JsonImageWrapper;
import ret.appcore.model.wrappers.JsonWrapper;
import ret.webscrapers.http.HttpClient;
import ret.webscrapers.http.HttpResponse;
import ret.webscrapers.http.ResponseType;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class InzeratAndImageSenderHandler extends SenderHandlerBase {

    private static Logger log = LogManager.getLogger(InzeratAndImageSenderHandler.class);

    public InzeratAndImageSenderHandler(HttpClient httpClient, String url) {
        super(httpClient, url);
    }


    @Override
    public SendResult send(JsonWrapper jsonWrapper) {

        JsonImageWrapper jsonImgWrapper = (JsonImageWrapper) jsonWrapper;

        if (jsonImgWrapper.getJson().isPresent()) {
            RequestBody requestBody = buildRequestBody(jsonImgWrapper.getJson().get(), jsonImgWrapper.getBufferedImages());
            Request request = new Request.Builder().url(url).post(requestBody).build();
            Supplier<HttpResponse<String>> requestCode = () -> httpClient.makeRequest(request, String.class, true);
            HttpResponse<String> response = makeRequest(100, 1000, requestCode);
            log.info("Got response: {}", response.getResponseType());
            boolean success = response.getResponseType() == ResponseType.SUCCESS;
            return new SendResult(success, jsonImgWrapper);
        }
        return new SendResult(false, jsonImgWrapper);
    }


    private RequestBody buildRequestBody(String json, List<BufferedImage> images) {

        MultipartBody.Builder builder = new MultipartBody.Builder();
        RequestBody jsonBodyPart = RequestBody.create(MediaType.parse("application/json"), json);
        builder.addFormDataPart("json_file", "inzerat_json", jsonBodyPart);

        if (images.isEmpty()) {
            return builder.build();
        } else {
            for (int i = 0; i < images.size(); i++) {
                BufferedImage image = images.get(i);
                Optional<byte[]> bytes = imageToByteArr(image);
                if(bytes.isPresent()) {
                    String fileName = "img_" + i;
                    builder.addFormDataPart("img_file", fileName, RequestBody.create(MediaType.parse("image/jpeg"), bytes.get()));
                }
            }

            return builder.build();
        }
    }

    private Optional<byte[]> imageToByteArr(BufferedImage img) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(img, "jpeg", baos);
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
        return Optional.ofNullable(baos.toByteArray());
    }

}
