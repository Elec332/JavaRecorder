package nl.elec332.util.javarecorder.modifiers;

import nl.elec332.util.javarecorder.AbstractDelegatedRecorder;
import nl.elec332.util.javarecorder.api.IRecorder;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * Created by Elec332 on 12-4-2020
 */
public class ThreadedRecorder<T> extends AbstractDelegatedRecorder<T> {

    public ThreadedRecorder(IRecorder<T> parent) {
        super(parent);
        this.unProcessed = new ConcurrentLinkedQueue<>();
        this.processed = new ConcurrentLinkedQueue<>();
        this.stopped = new AtomicBoolean(false);
    }

    private final Queue<Supplier<T>> unProcessed;
    private final Queue<T> processed;
    private final AtomicBoolean stopped;

    @Override
    public void startRecorder(int fps, int width, int height, File file) {
        if (isRecording() || stopped.get()) {
            throw new IllegalStateException();
        }
        this.parent.startRecorder(fps, width, height, file);
        Thread preProcessor = new Thread(() -> {
            while (true) {
                synchronized (unProcessed) {
                    while (unProcessed.isEmpty()) {
                        try {
                            unProcessed.wait();
                        } catch (InterruptedException e) {
                            break;
                        }
                        if (stopped.get()) {
                            break;
                        }
                    }
                }
                if (!stopped.get()) {
                    Supplier<T> current;
                    long l = System.currentTimeMillis();
                    while ((current = unProcessed.poll()) != null) {
                        processed.add(current.get());
                        if (System.currentTimeMillis() - l > 2000) {
                            synchronized (processed) {
                                processed.notifyAll();
                            }
                        }
                    }
                }
                synchronized (processed) {
                    processed.notifyAll();
                }
                if (stopped.get()) {
                    return;
                }
            }
        }, "ImageTransformer");
        new Thread(() -> {
            while (true) {
                synchronized (processed) {
                    while (processed.isEmpty()) {
                        try {
                            processed.wait();
                        } catch (InterruptedException e) {
                            break;
                        }
                        if (stopped.get()) {
                            break;
                        }

                    }
                    T current;
                    while ((current = processed.poll()) != null) {
                        try {
                            this.parent.encodeFrame(current);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                    if (stopped.get()) {
                        unProcessed.clear();
                        preProcessor.interrupt();
                        parent.stopRecorder();
                        processed.clear();
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        System.gc();
                        stopped.set(false);
                        return;
                    }
                }
            }
        }, "Encoder").start();
        preProcessor.start();
    }

    @Override
    public void stopRecorder() {
        System.out.println("STOPRECORDWER");
        stopped.set(true);
        synchronized (unProcessed) {
            unProcessed.notifyAll();
        }
        System.out.println("STOPRECORDWERDONE");
    }

    @Override
    public boolean isRecording() {
        if (stopped.get()) {
            return false;
        }
        return this.parent.isRecording() && !stopped.get();
    }

    @Override
    public void encodeFrame(T frame) {
        if (stopped.get()) {
            return;
        }
        this.parent.encodeFrame(frame);
    }

    @Override
    public void encodeFrame(Supplier<T> frame) {
        if (frame == null || stopped.get()) {
            return;
        }
        unProcessed.add(frame);
        synchronized (unProcessed) {
            unProcessed.notifyAll();
        }
    }

    @Override
    public Supplier<T> convertFrame(BufferedImage image) {
        ColorModel cm = image.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = image.getData().createCompatibleWritableRaster();
        image.copyData(raster);
        return this.parent.convertFrame(new BufferedImage(cm, raster, isAlphaPremultiplied, null));
    }

}
