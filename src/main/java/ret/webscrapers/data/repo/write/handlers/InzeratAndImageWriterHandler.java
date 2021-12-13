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

package ret.webscrapers.data.repo.write.handlers;



import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ret.appcore.model.wrappers.JsonImageWrapper;
import ret.appcore.model.wrappers.JsonWrapper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class InzeratAndImageWriterHandler extends WriterHandlerBase {

    private static Logger log = LogManager.getLogger(InzeratAndImageWriterHandler.class);
    private final String baseImageDir;

    public InzeratAndImageWriterHandler(String baseDataDir, String baseImageDir) {
        super(baseDataDir);
        this.baseImageDir = baseImageDir;
    }


    @Override
    public boolean writeDataToDisc(JsonWrapper wrapper) {

        JsonImageWrapper jsonImageWrapper = (JsonImageWrapper) wrapper;

        // write data
        boolean isSuccessful = super.writeDataToDisc(jsonImageWrapper);

        // Write images
        isSuccessful = writeImagesToDisc(jsonImageWrapper, isSuccessful);

        return isSuccessful;
    }


    private boolean writeImagesToDisc(JsonImageWrapper wrapper, boolean isSuccessful) {
        File dir = new File(baseImageDir + File.separator + wrapper.getUuid());
        ensureDirExists(dir);

        for (int i = 0; i < wrapper.getBufferedImages().size(); i++) {
            BufferedImage bufferedImage = wrapper.getBufferedImages().get(i);
            String imgName = "img_" + i;
            File imgFile = new File(dir + File.separator + imgName + ".jpeg");
            try {
                ImageIO.write(bufferedImage, "jpg", imgFile);
            } catch (IOException e) {
                isSuccessful = false;
                log.error("Error writing image to disc: {}", imgFile.toString(), e);
            }
        }
        return isSuccessful;
    }


}
