package nl.elec332.util.javarecorder.ffmpeg;

import nl.elec332.util.implementationmanager.api.ImplementationType;
import nl.elec332.util.javarecorder.api.IRecorder;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameRecorder;
import org.bytedeco.javacv.Java2DFrameConverter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * Created by Elec332 on 13-4-2020
 */
public class FFmpegRecorder implements IRecorder<Frame> {

    public FFmpegRecorder() {
        this.lock = new ReentrantLock();
        this.converter = new Java2DFrameConverter();
    }

    protected final ReentrantLock lock;
    private final Java2DFrameConverter converter;
    private FFmpegFrameRecorder recorder;
    protected int width, height;

    @Override
    public ImplementationType getImplementationType() {
        return ImplementationType.NATIVE;
    }

    @Override
    public void startRecorder(int fps, int width, int height, File file) {
        if (recorder != null) {
            throw new IllegalStateException();
        }
        lock.lock();
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(file, width, height);
        recorder.setFrameRate(fps);
        recorder.setFormat("mp4");
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        try {
            recorder.start();
            this.width = width;
            this.height = height;
            this.recorder = recorder;
        } catch (FrameRecorder.Exception e) {
            throw new RuntimeException(e);
        }
        lock.unlock();
    }

    @Override
    public void stopRecorder() {
        this.lock.lock();
        if (this.recorder == null) {
            throw new IllegalStateException();
        }
        try {
            recorder.stop();
            recorder.close();
            recorder = null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.lock.unlock();
    }

    @Override
    public boolean isRecording() {
        return recorder != null;
    }

    @Override
    public void encodeFrame(Frame frame) {
        this.lock.lock();
        try {
            recorder.record(frame);
        } catch (FrameRecorder.Exception e) {
            throw new RuntimeException(e);
        }
        this.lock.unlock();
    }

    @Override
    public Supplier<Frame> convertFrame(BufferedImage image) {
        return () -> converter.convert(IRecorder.reformatImage(image, BufferedImage.TYPE_3BYTE_BGR, width, height));
    }

    static {
        Loader.load(org.bytedeco.ffmpeg.presets.avformat.class);
    }

}