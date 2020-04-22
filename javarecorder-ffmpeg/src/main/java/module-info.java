import nl.elec332.util.javarecorder.api.IRecorder;
import nl.elec332.util.javarecorder.ffmpeg.FFmpegRecorder;

/**
 * Created by Elec332 on 21-4-2020
 */
module nl.elec332.util.javarecorder.ffmpeg {

    provides IRecorder with FFmpegRecorder;

    requires java.desktop;
    requires transitive org.bytedeco.ffmpeg;
    requires transitive nl.elec332.util.javarecorder;
    requires transitive nl.elec332.implementationmanager;

    requires static org.bytedeco.javacv;

}