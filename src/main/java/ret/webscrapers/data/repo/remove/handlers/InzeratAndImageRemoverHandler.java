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

package ret.webscrapers.data.repo.remove.handlers;

import ret.webscrapers.data.model.DataFile;

import java.io.File;
import java.util.Optional;

public class InzeratAndImageRemoverHandler extends RemoverHandlerBase {

    @Override
    public boolean remove(DataFile dataFile) {

        boolean removed = super.remove(dataFile);

        Optional<File> imageDirOp = dataFile.getImagesDir();
        if (imageDirOp.isPresent()) {

            File imgsDir = imageDirOp.get();

            if (imgsDir.exists()) {
                for (File file : imgsDir.listFiles()) {
                    file.delete();
                }
            }

            if (imgsDir.listFiles().length == 0) {
                removed = imgsDir.delete();
            }

        }

        return removed;
    }
}
