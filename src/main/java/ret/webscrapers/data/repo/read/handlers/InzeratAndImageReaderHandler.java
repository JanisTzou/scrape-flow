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

package ret.webscrapers.data.repo.read.handlers;


import ret.appcore.model.wrappers.JsonImageWrapper;
import ret.webscrapers.data.model.DataFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InzeratAndImageReaderHandler extends ReaderHandlerBase implements ReaderHandler {

    @Override
    public JsonImageWrapper read(DataFile dataFile) {
        Optional<String> jsonOp = readJson(dataFile.getDataFile());
        List<BufferedImage> imageList = new ArrayList<>();
        if (jsonOp.isPresent() && dataFile.getImagesDir().isPresent()) {
            File imgsDir = dataFile.getImagesDir().get();
            if (imgsDir.exists()) {
                for (File file : imgsDir.listFiles()) {
                    if (file.getName().contains(".jpeg")) {
                        try {
                            BufferedImage bufferedImage = ImageIO.read(file);
                            imageList.add(bufferedImage);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return new JsonImageWrapper(dataFile.getDataType(), dataFile.getUuid(), jsonOp, imageList);
    }
}
