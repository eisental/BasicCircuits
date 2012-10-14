
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
 */
public class ramwatch extends Circuit {
    private Ram ram;
    private RamListener ramListener;
    private BitSet7 ramaddr;
    
    private PulseOff pulseOff = new PulseOff();
    
    @Override
    public void inputChange(int inIdx, boolean state) {
    }
    
    class RamWatchListener implements RamListener {
        @Override
        public void dataChanged(Ram ram, BitSet7 address, BitSet7 data) {
            if (inputBits.get(0) & (ramaddr == null || ramaddr.equals(address))) {
                sendOutput(0, true);
                redstoneChips.getServer().getScheduler().scheduleSyncDelayedTask(redstoneChips, pulseOff, 1);
            }
        }
    }
    
    @Override
    protected boolean init(CommandSender sender, String[] args) {
        if (args.length >= 1) {
            if (args[0].startsWith("$")) {
                try {
                    if (args.length >= 2)
                        ramaddr = BitSetUtils.intToBitSet(Integer.decode(args[1]));
                    else
                        ramaddr = null;
                    
                    ram = (Ram)Memory.getMemory(args[0].substring(1), Ram.class);
                } catch (IllegalArgumentException e) {
                    error(sender, e.getMessage());
                } catch (IOException e) {
                    error(sender, e.getMessage());
                }
            } else error(sender, "Invalid argument: " + args[0]);
        } else {
            error(sender, "Expected at least one argument, the ram to watch.");
            return false;
        }
        
        if (inputs.length < 1 || outputs.length < 1) {
            error(sender, "Expected at least one input and output. Found " + inputs.length + " input(s) and " + outputs.length + " output(s).");
            return false;
        }
        
        if (sender!=null) resetOutputs();
        
        if (ram != null) {
            ramListener = new RamWatchListener();
            ram.addListener(ramListener);
            
            if (ramaddr==null)
                info(sender, "Created ram watcher targeting entirety of $"+ram.getId());
            else
                info(sender, "Created ram watcher targeting $"+ram.getId()+"@"+Integer.toHexString(BitSetUtils.bitSetToUnsignedInt(ramaddr, 0, ramaddr.length())));
        } else {
            error(sender, "Couldn't find ram to watch.");
            return false;
        }
        return true;
    }
    @Override
    protected void circuitShutdown() {
        if (ram != null)
           ram.getListeners().remove(ramListener);
    }
    
    class PulseOff implements Runnable {
        @Override
        public void run() {
            sendOutput(0, false);
        }
    }
    
    @Override
    protected boolean isStateless() {
        return false;
    }
}
