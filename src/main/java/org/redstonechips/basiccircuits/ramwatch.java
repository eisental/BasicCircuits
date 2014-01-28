
package org.redstonechips.basiccircuits;

import java.io.IOException;
import org.redstonechips.chip.Circuit;
import org.redstonechips.memory.Memory;
import org.redstonechips.memory.Ram;
import org.redstonechips.memory.RamListener;

/**
 *
 */
public class ramwatch extends Circuit {
    private Ram ram;
    private RamListener ramListener;
    private long ramaddr;
    
    @Override
    public void input(boolean state, int inIdx) {
        if (inIdx==0) {
            debug("Chip " + (state?"enabled":"disabled") + ".");
        }
    }
    
    class RamWatchListener implements RamListener {
        @Override
        public void dataChanged(Ram ram, long address, boolean[] data) {
            if (inputs[0] && (ramaddr == -1 || address == ramaddr)) {
                write(true, 0);
		write(false, 0);
            }
        }
    }
    
    @Override
    public Circuit init(String[] args) {
        if (args.length >= 1) {
            if (args[0].startsWith("$")) {
                try {
                    if (args.length >= 2)
                        ramaddr = Long.decode(args[1]);
                    else
                        ramaddr = -1;
                    
                    ram = (Ram)Memory.getMemory(args[0].substring(1), Ram.class);
                } catch (NumberFormatException | IOException e) {
                    return error(e.getMessage());
                }
            } else return error("Invalid argument: " + args[0]);
        } else return error("Expected at least one argument, the memory id to watch.");
        
        if (inputlen < 1 || outputlen < 1) 
            return error("Expected at least one input and output. Found " + inputlen + " input(s) and " + outputlen + " output(s).");
        
        if (activator!=null) clearOutputs();
        
        if (ram != null) {
            ramListener = new RamWatchListener();
            ram.addListener(ramListener);
            
            if (ramaddr==-1)
                info("Created ram watcher targeting any address of memory $"+ram.getId() + ".");
            else
                info("Created ram watcher targeting $"+ram.getId()+"@"+Long.toHexString(ramaddr) + ".");
            return this;
        } else 
            return error("Couldn't find ram to watch.");
    }
    
    @Override
    public void shutdown() {
        if (ram != null) {
            ram.getListeners().remove(ramListener);
            ram.release();
        }
    }
    
    @Override
    public boolean isStateless() {
        return false;
    }
}
