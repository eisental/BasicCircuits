
package org.tal.basiccircuits;

import java.io.IOException;
import org.bukkit.command.CommandSender;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.bitset.BitSet7;
import org.tal.redstonechips.bitset.BitSetUtils;
import org.tal.redstonechips.memory.Memory;
import org.tal.redstonechips.memory.Ram;
import org.tal.redstonechips.memory.RamListener;

/**
 *
 * @author Tal Eisenberg
 */
public class dregister extends Circuit {
    private static final int clockIdx = 0;
    private static final int resetIdx = 1;
    
    private Ram ram;
    private RamListener ramListener;
    private BitSet7 ramaddr;
    
    @Override
    public void inputChange(int inIdx, boolean state) {
        if (inIdx==resetIdx && state) {
            if (ram != null)
                ram.write(ramaddr, BitSetUtils.clearBitSet); //this will update the output
            else this.sendBitSet(BitSetUtils.clearBitSet);
        } else if (inputBits.get(clockIdx)) {
            if (ram != null)
                ram.write(ramaddr, inputBits.get(2, outputs.length+2)); //this will update the output
            else this.sendBitSet(inputBits.get(2, outputs.length+2));
        }
    }
    
    class DRegisterRamListener implements RamListener {
        @Override
        public void dataChanged(Ram ram, BitSet7 address, BitSet7 data) {
            if (ramaddr.equals(address))
                sendBitSet(data);
        }
    }
    
    @Override
    protected boolean init(CommandSender sender, String[] args) {
        if (args.length >= 1)
            if (args[0].startsWith("$")) {
                try {
                    if (args.length >= 2)
                        ramaddr = BitSetUtils.intToBitSet(Integer.decode(args[1]));
                    else
                        ramaddr = BitSetUtils.clearBitSet;
                    
                    ram = (Ram)Memory.getMemory(args[0].substring(1), Ram.class);
                } catch (IllegalArgumentException e) {
                    error(sender, e.getMessage());
                } catch (IOException e) {
                    error(sender, e.getMessage());
                }
            } /*else if (channel==null) {
                if (args[0].startsWith("#"))
                    channel = args[0].substring(1);
                else channel = args[0];
            } */ else error(sender, "Invalid argument: " + args[0]);
        
        if (inputs.length!=outputs.length+2) {
            sender.sendMessage("Expecting 2 more inputs than outputs. Found " + inputs.length + " input(s) and " + outputs.length + " output(s).");
            return false;
        }
        if (sender!=null) resetOutputs();
        
        if (ram != null) {
            ramListener = new DRegisterRamListener();
            ram.addListener(ramListener);
            this.sendBitSet(ram.read(ramaddr));
            info(sender, "Created "+outputs.length+"-bit register backed by memory: "+ram.getId()+"@"+Integer.toHexString(BitSetUtils.bitSetToUnsignedInt(ramaddr, 0, ramaddr.length())));
        } else
            info(sender, "Created "+outputs.length+"-bit register.");
        return true;
    }
    @Override
    protected void circuitShutdown() {
        if (ram != null)
           ram.getListeners().remove(ramListener);
    }
    @Override
    protected boolean isStateless() {
        return false;
    }
}
