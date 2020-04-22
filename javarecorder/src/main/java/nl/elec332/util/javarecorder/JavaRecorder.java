package nl.elec332.util.javarecorder;

import nl.elec332.util.implementationmanager.ImplementationManager;
import nl.elec332.util.javarecorder.api.IRecorder;
import nl.elec332.util.javarecorder.modifiers.DecimatedRecorder;
import nl.elec332.util.javarecorder.modifiers.ThreadedRecorder;

import java.util.*;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Created by Elec332 on 21-4-2020
 */
public class JavaRecorder {

    private static final IRecorder<?> FASTEST_RECORDER;
    private static final Collection<IRecorder<?>> ALL_RECORDERS;

    public static IRecorder<?> getFastestRecorder() {
        return FASTEST_RECORDER;
    }

    public static Collection<IRecorder<?>> getAllRecorders() {
        return ALL_RECORDERS;
    }

    @SafeVarargs
    public static <T> IRecorder<T> modifyRecorder(IRecorder<T> recorder, Function<IRecorder<T>, IRecorder<T>>... modifiers) {
        IRecorder<T> ret = recorder;
        for (Function<IRecorder<T>, IRecorder<T>> mod : modifiers) {
            ret = mod.apply(ret);
        }
        return ret;
    }

    @SafeVarargs
    public static <T> UnaryOperator<IRecorder<T>> createModifier(Function<IRecorder<T>, IRecorder<T>>... modifiers) {
        Function<IRecorder<T>, IRecorder<T>> ret = UnaryOperator.identity();
        for (Function<IRecorder<T>, IRecorder<T>> func : modifiers) {
            ret = ret.andThen(func);
        }
        return ret::apply;
    }

    public static <T> IRecorder<T> threaded(IRecorder<T> recorder) {
        return new ThreadedRecorder<>(recorder);
    }

    public static <T> IRecorder<T> decimated(IRecorder<T> recorder) {
        return decimated(recorder, 30);
    }

    public static <T> IRecorder<T> decimated(IRecorder<T> recorder, int maxFps) {
        return new DecimatedRecorder<>(recorder, maxFps);
    }

    static {
        FASTEST_RECORDER = ImplementationManager.loadService(IRecorder.class);
        Set<IRecorder<?>> recorder = new HashSet<>();
        ImplementationManager.loadServices(IRecorder.class).forEach(recorder::add);
        ALL_RECORDERS = Collections.unmodifiableCollection(recorder);
    }

}
