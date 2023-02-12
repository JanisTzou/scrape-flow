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

package com.github.scrape.images;

import lombok.Data;
import lombok.Getter;
import lombok.ToString;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


public class ShapeTest {

    @Test
    public void test() throws IOException {
        write("8");
        for (int i = 0; i < 1; i++) {
            generateShape();
        }
    }

    public void generateShape() throws IOException {
//        BufferedImage bufferedImg = ImageIO.read(new File("/Users/janis/Desktop/aladin_meteogram.png"));
        BufferedImage bufferedImg = ImageIO.read(new File("/Users/janis/Desktop/meteoblue_meteogram.png"));
        RGB backgroundThreshold = new RGB(248, 248, 248);
        Image image = new Image(bufferedImg, backgroundThreshold);
        Shapes shapes = new Shapes(image.getWidth(), image.getHeight());

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Pixel pixel = image.getPixel(x, y);
                shapes.addShape(pixel);
            }
        }


        AtomicInteger counter = new AtomicInteger(0);
        shapes.get().stream().limit(20000).forEach(
                shape -> write(shape, counter.incrementAndGet())
        );

        System.out.println(shapes.count());
    }

    private void write(Shape shape, int no) {
        BufferedImage image = new BufferedImage(shape.getWidth(), shape.getHeight(), BufferedImage.TYPE_INT_RGB);
        paintWhite(image);

        int minX = shape.getMinX();
        int minY = shape.getMinY();

        shape.pixels.forEach(pixel -> {
            int r = pixel.getRGB().getRed(); // red component 0...255
            int g = pixel.getRGB().getGreen(); // green component 0...255
            int b = pixel.getRGB().getBlue(); // blue component 0...255
            int col = (r << 16) | (g << 8) | b;
            image.setRGB(pixel.getX() - minX, pixel.getY() - minY, col);
        });

        try {
            ImageIO.write(image, "jpeg", new File("/Users/janis/Desktop/shapes/shape" + no + ".jpg"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public BufferedImage write(String text) {
        BufferedImage image = new BufferedImage(20, 20, BufferedImage.TYPE_INT_RGB);
        Font font = new Font("Serif", Font.PLAIN, 15);

        paintWhite(image);

        Graphics g = image.getGraphics();
        g.setFont(font);
        g.setColor(Color.BLACK);
        g.drawString(text, 5, 15);

        try {
            ImageIO.write(image, "jpeg", new File("/Users/janis/Desktop/letter.jpg"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return image;
    }

    private void paintWhite(BufferedImage image) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int r = 255; // red component 0...255
                int g = 255; // green component 0...255
                int b = 255; // blue component 0...255
                int col = (r << 16) | (g << 8) | b;
                image.setRGB(x, y, col);
            }
        }
    }

    public interface Colorful {

        RGB getRGB();

    }

    public static class Image {

        private final Pixel[][] pixels;

        @Getter
        private final int height;

        @Getter
        private final int width;

        private final RGB backgroundThreshold;

        public Image(BufferedImage image, RGB backgroundThreshold) {
            this.pixels = new Pixel[image.getWidth()][image.getHeight()];
            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    Pixel pixel = new Pixel(x, y, getRGB(image, x, y), backgroundThreshold);
                    pixels[x][y] = pixel;
                }
            }
            this.height = image.getHeight();
            this.width = image.getWidth();
            this.backgroundThreshold = backgroundThreshold;
        }

        public Pixel getPixel(int x, int y) {
            return this.pixels[x][y];
        }

        public RGB getRGB(BufferedImage image, int x, int y) {
            int clr = image.getRGB(x, y);
            int red = (clr & 0x00ff0000) >> 16;
            int green = (clr & 0x0000ff00) >> 8;
            int blue = clr & 0x000000ff;
            return new RGB(red, green, blue);
        }

    }

    public static class Shapes {

        private final int width;
        private final int height;
        private final Shape[][] shapeMap;
        private final List<Shape> shapes = new ArrayList<>();

        public Shapes(int width, int height) {
            this.width = width;
            this.height = height;
            this.shapeMap = new Shape[width][height];
        }

        Optional<Shape> getShape(Pixel pixel) {
            return Optional.ofNullable(shapeMap[pixel.x][pixel.y]);
        }

        void addToMap(Pixel pixel, Shape shape) {
            shapeMap[pixel.x][pixel.y] = shape;
        }

        void addNewShape(Pixel pixel, Shape shape) {
            shapes.add(shape);
            shapeMap[pixel.x][pixel.y] = shape;
        }

        private void addShape(Pixel pixel) {
            if (!pixel.isBackground()) {

                List<Shape> adjacentShapes = getAdjacentShapes(pixel);
                boolean addedToShape = false;
                for (Shape shape : adjacentShapes) {
                    if (pixel.getRGB().equals(shape.getRGB())) {
                        shape.add(pixel);
                        addToMap(pixel, shape);
                        addedToShape = true;
                        break;
                    }
                }
                if (!addedToShape) {
                    Shape shape = new Shape(pixel);
                    addNewShape(pixel, shape);
                }

            }
        }

        private List<Shape> getAdjacentShapes(Pixel pixel) {

            List<Shape> adjacents = new ArrayList<>();

            // left
            if (pixel.x > 0) {
                // left
                add(adjacents, shapeMap[pixel.x - 1][pixel.y]);

                // above left
                if (pixel.y > 0) {
                    add(adjacents, shapeMap[pixel.x - 1][pixel.y - 1]);
                }

//            // below left
//            if (pixel.y < height - 1) {
//                add(adjacents, shapeMap[pixel.x - 1][ pixel.y + 1]);
//            }
            }

            // right
            if (pixel.x < width - 1) {
//            // right
//            add(adjacents, shapeMap[pixel.x + 1][ pixel.y]);

                // above right
                if (pixel.y > 0) {
                    add(adjacents, shapeMap[pixel.x + 1][pixel.y - 1]);
                }

//            // below right
//            if (pixel.y < height - 1) {
//                add(adjacents, shapeMap[pixel.x + 1][pixel.y + 1]);
//            }
            }

            // above
            if (pixel.y > 0) {
                add(adjacents, shapeMap[pixel.x][pixel.y - 1]);
            }

//        // below
//        if (pixel.y < height - 1) {
//            add(adjacents, shapeMap[pixel.x][pixel.y + 1]);
//        }

            return adjacents;
        }

        private void add(List<Shape> adjacents, Shape shape) {
            if (shape != null) {
                adjacents.add(shape);
            }
        }

        public List<Shape> get() {
            return Collections.unmodifiableList(this.shapes);
        }

        public long count() {
            return shapes.size();
        }

    }

    public static class Shape implements Colorful {
        List<Pixel> pixels = new ArrayList<>();
        Integer height;
        Integer width;

        public Shape(Pixel pixel) {
            this.pixels.add(pixel);
        }

        public void add(Pixel pixel) {
            this.pixels.add(pixel);
        }

        @Override
        public RGB getRGB() {
            return this.pixels.get(0).getRGB();
        }

        public int getMinX() {
            return pixels.stream().mapToInt(Pixel::getX).min().orElse(-1);
        }

        public int getMaxX() {
            return pixels.stream().mapToInt(Pixel::getX).max().orElse(-1);
        }

        public int getMinY() {
            return pixels.stream().mapToInt(Pixel::getY).min().orElse(-1);
        }

        public int getMaxY() {
            return pixels.stream().mapToInt(Pixel::getY).max().orElse(-1);
        }

        public int getHeight() {
            if (height == null) {
                int min = pixels.stream().mapToInt(Pixel::getY).min().orElse(-1);
                int max = pixels.stream().mapToInt(Pixel::getY).max().orElse(-1);
                height = max - min + 1;
            }
            return height;
        }

        public int getWidth() {
            if (width == null) {
                int min = pixels.stream().mapToInt(Pixel::getX).min().orElse(-1);
                int max = pixels.stream().mapToInt(Pixel::getX).max().orElse(-1);
                width = max - min + 1;
            }
            return width;
        }
    }

    @Getter
    @ToString
    public static class Pixel implements Colorful {
        private final int x;
        private final int y;
        private final RGB rgb;
        private final boolean background;

        public Pixel(int x, int y, RGB rgb, RGB backgroundThreshold) {
            this.x = x;
            this.y = y;
            this.rgb = rgb;
            this.background = backgroundThreshold.red <= rgb.red
                    && backgroundThreshold.green <= rgb.green
                    && backgroundThreshold.blue <= rgb.blue;
        }


        public boolean isBackground() {
            return this.background;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Pixel)) return false;
            Pixel pixel = (Pixel) o;
            return x == pixel.x && y == pixel.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }

        @Override
        public RGB getRGB() {
            return rgb;
        }
    }

    @Data
    public static class RGB {
        private final int red;
        private final int green;
        private final int blue;
    }


}
