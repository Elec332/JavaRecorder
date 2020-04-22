package nl.elec332.util.javarecorder;

import nl.elec332.util.implementationmanager.api.ImplementationType;
import nl.elec332.util.javarecorder.api.IRecorder;

/**
 * Created by Elec332 on 22-4-2020
 */
public abstract class AbstractDelegatedRecorder<T> implements IRecorder<T> {

    public AbstractDelegatedRecorder(IRecorder<T> parent) {
        this.parent = parent;
    }

    protected final IRecorder<T> parent;

    @Override
    public ImplementationType getImplementationType() {
        return parent.getImplementationType();
    }

    @Override
    public int getImplementationSpeed() {
        return parent.getImplementationSpeed();
    }

}
