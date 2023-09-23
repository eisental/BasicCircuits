package org.redstonechips.basiccircuits;


import java.util.Random;

import org.redstonechips.circuit.Circuit;


/**
 *
 * @author Tal Eisenberg
 */
public class random extends Circuit {
    private final Random randomGen = new Random();
    private boolean setAll = false;

    @Override
    public void input(boolean state, int inIdx) {
        if (state) { // to high. set matching output pin to random value.
            if (inIdx==0 && setAll) {
                for (int i=0; i<outputlen; i++)
                    write(randomGen.nextBoolean(), i);
            } else
                write(randomGen.nextBoolean(), inIdx);
        } else { // to low. set matching output pin to low.
            if (inIdx==0 && setAll) {
                for (int i=0; i<outputlen; i++)
                    write(false, i);
            } else
                write(false, inIdx);
        }
    }

    @Override
    public Circuit init(String[] args) {
        if (inputlen==1 && outputlen!=1) {
            setAll = true;
        } else if (inputlen==outputlen) {
            setAll = false;
        } else
            return error("Expecting either the same amount of inputs and outputs, or exactly 1 input.");

        return this;
    }
}
