import nl.elec332.util.javarecorder.api.IRecorder;
import nl.elec332.util.javarecorder.impl.JCodecRecorder;

/**
 * Created by Elec332 on 21-4-2020
 */
module nl.elec332.util.javarecorder {

    exports nl.elec332.util.javarecorder;
    exports nl.elec332.util.javarecorder.api;
    exports nl.elec332.util.javarecorder.modifiers;

    uses IRecorder;

    provides IRecorder with JCodecRecorder;

    requires java.desktop;
    requires nl.elec332.implementationmanager;
    requires static jcodec;

}