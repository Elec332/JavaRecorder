package nl.elec332.util.javarecorder.impl;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import nl.elec332.util.implementationmanager.api.ImplementationType;
import nl.elec332.util.javarecorder.api.IRecorder;
import org.jcodec.api.SequenceEncoder;
import org.jcodec.common.Codec;
import org.jcodec.common.Format;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.io.SeekableByteChannel;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture;
import org.jcodec.common.model.Rational;

/**
 * Created by Elec332 on 13-4-2020
 */
public class JCodecRecorder implements IRecorder<Picture> {

    public JCodecRecorder() {
        this.recorder = null;
        this.recOut = null;
        this.lock = new ReentrantLock();
    }

    protected final ReentrantLock lock;
    protected SeekableByteChannel recOut;
    protected SequenceEncoder recorder;
    protected int width, height;

    @Override
    public ImplementationType getImplementationType() {
        return ImplementationType.JAVA_DEFAULT;
    }

    @Override
    public void startRecorder(int fps, int width, int height, File file) {
        if (recOut != null) {
            throw new IllegalStateException();
        }
        lock.lock();
        try {
            recOut = NIOUtils.writableChannel(file);
            this.width = width;
            this.height = height;
            this.recorder = new SequenceEncoder(recOut, Rational.R(fps, 1), Format.MOV, Codec.H264, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        lock.unlock();
    }

    @Override
    public void stopRecorder() {
        if (recOut == null) {
            throw new IllegalStateException();
        }
        lock.lock();
        try {
            recorder.finish();
            this.recorder = null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            NIOUtils.closeQuietly(recOut);
            recOut = null;
        }
        lock.unlock();
    }

    @Override
    public void encodeFrame(Picture frame) {
        if (this.recorder != null) {
            lock.lock();
            try {
                this.recorder.encodeNativeFrame(frame);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            lock.unlock();
        }
    }

    @Override
    public boolean isRecording() {
        return recorder != null;
    }

    @Override
    public Supplier<Picture> convertFrame(BufferedImage image) {
        return () -> fromBufferedImageRGB(IRecorder.reformatImage(image, BufferedImage.TYPE_INT_RGB, width, height));
    }

    public static Picture fromBufferedImageRGB(BufferedImage src) {
        Picture dst = Picture.create(src.getWidth(), src.getHeight(), ColorSpace.RGB);
        fromBufferedImage(src, dst);
        return dst;
    }

    public static void fromBufferedImage(BufferedImage src, Picture dst) {
        byte[] dstData = dst.getPlaneData(0);

        int off = 0;
        for (int i = 0; i < src.getHeight(); i++) {
            for (int j = 0; j < src.getWidth(); j++) {
                int rgb1 = src.getRGB(j, i);
                dstData[off++] = (byte) (((rgb1 >> 16) & 0xff) - 128);
                dstData[off++] = (byte) (((rgb1 >> 8) & 0xff) - 128);
                dstData[off++] = (byte) ((rgb1 & 0xff) - 128);
            }
        }
    }

}