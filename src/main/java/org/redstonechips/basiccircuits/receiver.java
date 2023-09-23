package org.redstonechips.basiccircuits;


import org.redstonechips.circuit.Circuit;
import org.redstonechips.util.BooleanSubset;
import org.redstonechips.wireless.Receiver;

/**
 *
 * @author Tal Eisenberg
 */
public class receiver extends Circuit {
    private int dataPin;
    private Receiver rec;

    @Override
    public void input(boolean state, int inIdx) {}

    @Override
    public Circuit init(String[] args) {
        if (outputlen==0) return error("Expecting at least 1 output pin.");

        if (args.length>0) {
            try {
                dataPin = (outputlen==1?0:1);
                rec = new ReceiverImpl();
                int len = outputlen-dataPin;
                rec.init(activator, args[0], len, this);
                return this;
            } catch (IllegalArgumentException ie) {
                return error(ie.getMessage());
            }
        } else return error("Channel name is missing.");
    }

    class ReceiverImpl extends Receiver {
        @Override
        public void receive(BooleanSubset bits) {
            if (chip.hasListeners()) debug("Received " + bits.toString());
            writeBooleanSubset(bits, dataPin, outputlen-dataPin);
            if (outputlen>1) {
                write(true, 0);
                write(false, 0);
            }
        }
    }

    @Override
    public boolean isStateless() {
        return false;
    }
}
