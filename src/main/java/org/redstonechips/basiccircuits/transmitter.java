package org.redstonechips.basiccircuits;


import java.util.ArrayList;
import java.util.List;

import org.redstonechips.circuit.Circuit;
import org.redstonechips.util.BooleanArrays;
import org.redstonechips.util.BooleanSubset;
import org.redstonechips.wireless.Transmitter;

/**
 *
 * @author Tal Eisenberg
 */
public class transmitter extends Circuit {
    private boolean selectMode = false;
    private int selectLength = 0;
    private int[] baseStartBit;

    private Transmitter[] modules;

    private BooleanSubset transmission;

    @Override
    public void input(boolean state, int inIdx) {
        if (inputlen==1) { // no clock pin and no select
            transmitInputs();
        } else { // has a clock pin
            if (selectMode) {
                int select = (int)BooleanArrays.toUnsignedInt(inputs, 1, selectLength);
                for (int i=0; i<modules.length; i++) {
                    modules[i].setStartBit(baseStartBit[i] + select*modules[i].getLength());
                }

            }

            if (inputs[0]) {
                transmitInputs();
            }
        }
    }

    private void transmitInputs() {
        if (chip.hasListeners()) debug("Transmitting " + transmission.length() + " bits: " + transmission.toPrettyString() + " on " + getChannelString());
        for (Transmitter t : modules)
            t.transmitSubset(transmission);
    }

    @Override
    public Circuit init(String[] args) {
        if (inputlen==0) return error("Expecting at least 1 input.");

        if (args.length>0) {
            List<String> smodules = new ArrayList<>();

            try {
                for (String arg : args) {
                    if (arg.toLowerCase().startsWith("select(") && arg.toLowerCase().endsWith(")")) {
                        String sselect = arg.substring(7, arg.length()-1);
                        try {
                            selectLength = Integer.decode(sselect);
                            if (inputlen<1+selectLength+1)
                                return error("Expecting at least " + (2+selectLength) + " inputs for select mode.");
                        } catch (NumberFormatException ne) {
                            return error("Bad select length argument: " + args[1]);
                        }
                        selectMode = true;
                    } else {
                        smodules.add(arg);
                    }
                }

                if (smodules.isEmpty()) return error("Can't find any channel names.");

                modules = new Transmitter[smodules.size()];
                baseStartBit = new int[modules.length];

                for (int i=0; i<modules.length; i++) {
                    try {
                        Transmitter t = new Transmitter();
                        int len;
                        if (inputlen == 1)len = 1;
                        else len = inputlen - 1 - selectLength;
                        t.init(activator, smodules.get(i), len, this);
                        modules[i]=t;
                        baseStartBit[i] = t.getStartBit();
                    } catch (IllegalArgumentException ie) {
                        return error(ie.getMessage());
                    }
                }


                if (selectMode) {
                    info("Inputs 1-" + (selectLength) + " are channel bit select pins.");
                }

                if (inputlen==1)
                    transmission = new BooleanSubset(inputs, 0, 1);
                else transmission = new BooleanSubset(inputs, 1+selectLength, inputlen-1-selectLength);

                return this;
            } catch (IllegalArgumentException ie) {
                return error(ie.getMessage());
            }
        } else
            return error("Channel sign argument is missing.");
    }

    private String getChannelString() {
        StringBuilder b = new StringBuilder();

        for (Transmitter t : modules) {
            b.append(t.getChannel().name).append(":").append(t.getStartBit()).append(", ");
        }
        b.delete(b.length()-2, b.length()-1);

        return b.toString();
    }
}
