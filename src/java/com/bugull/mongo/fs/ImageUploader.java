/**
 * Copyright (c) www.bugull.com
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bugull.mongo.fs;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSDBFile;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import javax.imageio.ImageIO;
import org.apache.log4j.Logger;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class ImageUploader extends Uploader{
    
    private final static Logger logger = Logger.getLogger(ImageUploader.class);
    
    private final static String DIMENSION = "dimension";
    
    public ImageUploader(File file, String fName){
        super(file, fName);
    }
    
    public ImageUploader(File file, String fName, String folderName){
        super(file, fName, folderName);
    }
    
    public void save(Watermark watermark){
        this.save();
        if(watermark != null){
            addWatermark(watermark);
        }
    }
    
    private InputStream getOriginalInputStream(){
        DBObject query = new BasicDBObject(BuguFS.FILENAME, filename);
        query.put(DIMENSION, null);
        GridFSDBFile f = fs.findOne(query);
        return f.getInputStream();
    }
    
    private void addWatermark(Watermark watermark) {
        //original image
        BufferedImage imageOriginal = null;
        try {
            imageOriginal = ImageIO.read(getOriginalInputStream());
        } catch (Exception ex) {
            logger.error(ex);
            return;
        }
        int widthOriginal = imageOriginal.getWidth(null);
        int heightOriginal = imageOriginal.getHeight(null);
        BufferedImage bufImage = new BufferedImage(widthOriginal, heightOriginal, imageOriginal.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : imageOriginal.getType());
        Graphics g = bufImage.createGraphics();
        g.drawImage(imageOriginal, 0, 0, widthOriginal, heightOriginal, null);
        //watermark image
        BufferedImage imageWaterMark = null;
        String watermarkFilePath = watermark.getFilePath();
        if(watermarkFilePath == null || watermarkFilePath.trim().equals("")){
            return;
        }
        try {
            imageWaterMark = ImageIO.read(new File(watermarkFilePath));
        } catch (Exception ex) {
            logger.error(ex);
            return;
        }
        int widthWaterMark = imageWaterMark.getWidth(null);
        int heightWaterMark = imageWaterMark.getHeight(null);
        if (widthOriginal < widthWaterMark || heightOriginal < heightWaterMark) {
            return;
        } 
        //position of the watermark
        switch(watermark.getAlign()){
            case Watermark.BOTTOM_RIGHT:
                g.drawImage(imageWaterMark, widthOriginal - widthWaterMark - watermark.getRight(), heightOriginal - heightWaterMark - watermark.getBottom(), widthWaterMark, heightWaterMark, null);
                break;
            case Watermark.CENTER:
                g.drawImage(imageWaterMark, (widthOriginal - widthWaterMark) / 2, (heightOriginal - heightWaterMark) / 2, widthWaterMark, heightWaterMark, null);
                break;
            default:
                break;
        }
        g.dispose();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(baos);
        JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(bufImage);
        param.setQuality(1f, false);
        try {
            encoder.setJPEGEncodeParam(param);
            encoder.encode(bufImage);
        } catch (Exception ex) {
            logger.error(ex);
        }
        fs.save(baos.toByteArray(), filename, folder, map);
        close(baos);
    }
    
    public void compress(String dimension, int maxWidth, int maxHeight) {
        String lower = filename.toLowerCase();
        boolean isTransparent = lower.endsWith(".png") || lower.endsWith(".gif");
        Image image = null;
        try {
            image = ImageIO.read(getOriginalInputStream());
        } catch (Exception ex) {
            logger.error(ex);
            return;
        }
        int srcWidth = image.getWidth(null);
        int srcHeight = image.getHeight(null);
        int newWidth = srcWidth;
        int newHeight = srcHeight;
        if (srcWidth > maxWidth || srcHeight > maxHeight) {
            float f = Math.max((float) srcWidth / maxWidth, (float) srcHeight / maxHeight);
            newWidth = Math.round(srcWidth / f);
            newHeight = Math.round(srcHeight / f);
        }
        BufferedImage bufImg = new BufferedImage(newWidth, newHeight, isTransparent ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
        bufImg.getGraphics().drawImage(image, 0, 0, newWidth, newHeight, null);
        bufImg.getGraphics().dispose();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (isTransparent) {
            try {
                ImageIO.write(bufImg, "png", baos);
            } catch (Exception ex) {
                logger.error(ex);
            }
        }
        else{
            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(baos);
            JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(bufImg);
            param.setQuality(1.0f, true);
            try {
                encoder.encode(bufImg, param);
            } catch (Exception ex) {
                logger.error(ex);
            }
        }
        setAttribute(DIMENSION, dimension);
        fs.save(baos.toByteArray(), filename, folder, map);
        close(baos);
    }
    
    private void close(ByteArrayOutputStream baos){
        try{
            baos.close();
        }catch(Exception e){
            logger.error(e.getMessage());
        }
    }
    
}
