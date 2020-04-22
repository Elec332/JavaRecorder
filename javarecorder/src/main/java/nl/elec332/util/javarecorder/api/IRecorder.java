package nl.elec332.util.javarecorder.api;

import nl.elec332.util.implementationmanager.api.IExtensionImplementation;
import nl.elec332.util.implementationmanager.api.ImplementationType;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.function.Supplier;

/**
 * Created by Elec332 on 12-4-2020
 */
public interface IRecorder<T> extends IExtensionImplementation {

    @Override
    ImplementationType getImplementationType();

    void startRecorder(int fps, int width, int height, File file);

    void stopRecorder();

    boolean isRecording();

    void encodeFrame(T frame);

    default void encodeFrame(Supplier<T> frame) {
        if (frame == null) {
            return;
        }
        encodeFrame(frame.get());
    }

    default void encodeFrame(BufferedImage frame) {
        encodeFrame(convertFrame(frame));
    }

    Supplier<T> convertFrame(BufferedImage image);

    static BufferedImage reformatImage(BufferedImage sourceImage, int targetType, int width, int height) {
        int newWidth = 0;
        int newHeight = 0;
        while (newHeight + sourceImage.getHeight() <= height) {
            newHeight += sourceImage.getHeight();
        }
        while (newWidth + sourceImage.getWidth() <= width) {
            newWidth += sourceImage.getWidth();
        }
        if (newWidth == 0) {
            newWidth = width;
        }
        if (newHeight == 0) {
            newHeight = height;
        }
        if (sourceImage.getType() == targetType && newHeight == sourceImage.getHeight() && newWidth == sourceImage.getWidth()) {
            return sourceImage;
        }
        BufferedImage image = new BufferedImage(width, height, targetType);
        image.getGraphics().drawImage(sourceImage, 0, 0, newWidth, newHeight, null);
        return image;
    }

}