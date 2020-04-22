package nl.elec332.util.javarecorder.modifiers;

import nl.elec332.util.implementationmanager.api.ImplementationType;
import nl.elec332.util.javarecorder.AbstractDelegatedRecorder;
import nl.elec332.util.javarecorder.api.IRecorder;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.function.Supplier;

/**
 * Created by Elec332 on 12-4-2020
 */
public class DecimatedRecorder<T> extends AbstractDelegatedRecorder<T> {

    public DecimatedRecorder(IRecorder<T> parent, int maxFps) {
        super(parent);
        this.maxFps = maxFps;
        this.counter = 0;
    }

    private final int maxFps;
    private int decimation, counter;

    @Override
    public ImplementationType getImplementationType() {
        return parent.getImplementationType();
    }

    @Override
    public int getImplementationSpeed() {
        return parent.getImplementationSpeed();
    }

    @Override
    public void startRecorder(int fps, int width, int height, File file) {
        int usedFps = Math.min(fps, this.maxFps);
        this.decimation = Math.floorDiv(fps, usedFps);
        this.parent.startRecorder(usedFps, width, height, file);
    }

    @Override
    public void stopRecorder() {
        this.parent.stopRecorder();
    }

    @Override
    public boolean isRecording() {
        return this.parent.isRecording();
    }

    @Override
    public void encodeFrame(T frame) {
        if (decimation <= 1) {
            this.parent.encodeFrame(frame);
            return;
        }
        if (counter == 0) {
            this.parent.encodeFrame(frame);
        }
        counter++;
        if (counter >= decimation) {
            counter = 0;
        }
    }

    @Override
    public void encodeFrame(Supplier<T> frame) {
        if (decimation <= 1) {
            this.parent.encodeFrame(frame);
            return;
        }
        if (counter == 0) {
            this.parent.encodeFrame(frame);
        }
        counter++;
        if (counter >= decimation) {
            counter = 0;
        }
    }

    @Override
    public Supplier<T> convertFrame(BufferedImage image) {
        return this.parent.convertFrame(image);
    }

}
