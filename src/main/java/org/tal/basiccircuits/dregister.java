
package org.tal.basiccircuits;

import org.bukkit.command.CommandSender;
import org.tal.redstonechips.circuit.Circuit;

/**
 *
 * @author Tal Eisenberg
 */
public class dregister extends Circuit {
    private static final int clockIdx = 0;
    private static final int resetIdx = 1;

    @Override
    public void inputChange(int inIdx, boolean state) {
        if (inIdx==clockIdx && state) {
            for (int i=0; i<outputs.length; i++) {
                sendOutput(i, inputBits.get(i+2));
            }
        } else if (inIdx==resetIdx && state) {
            outputBits.clear();
            this.sendBitSet(outputBits);
        }
    }

    @Override
    protected boolean init(CommandSender sender, String[] args) {
        if (inputs.length!=outputs.length+2) {
            sender.sendMessage("Expecting 2 more inputs than outputs. Found " + inputs.length + " input(s) and " + outputs.length + " output(s).");
            return false;
        } else
            return true;
    }

}
