
package org.redstonechips.basiccircuits;

import java.io.IOException;
import java.util.Arrays;
import org.redstonechips.circuit.Circuit;
import org.redstonechips.memory.Memory;
import org.redstonechips.memory.Ram;
import org.redstonechips.memory.RamListener;

/**
 *
 * @author Tal Eisenberg
 */
public class dregister extends Circuit {
    private static final int clockIdx = 0;
    private static final int resetIdx = 1;
    
    private Ram ram;
    private RamListener ramListener;
    private long ramaddr;
    private boolean[] register;
    private boolean[] clearRegister;
    
    @Override
    public void input(boolean state, int inIdx) {
        if (inIdx==resetIdx && state) {
            if (ram != null)
                ram.write(ramaddr, clearRegister); //this will update the output
            else this.writeBits(clearRegister);
        } else if (inputs[clockIdx]) {
            if (ram != null)
                ram.write(ramaddr, Arrays.copyOfRange(inputs, 2, inputlen)); //this will update the output
            else this.writeBits(inputs, 2, outputlen);
        }
    }
    
    class dregisterRamListener implements RamListener {
        @Override
        public void dataChanged(Ram ram, long address, boolean[] data) {
            if (ramaddr == address)
                writeBits(data);
        }
    }
    
    @Override
    public Circuit init(String[] args) {
        if (inputlen!=outputlen+2)
            return error("Expecting 2 more inputs than outputs. Found " + inputlen + " input(s) and " + outputlen + " output(s).");

        if (args.length >= 1) {
            if (args[0].startsWith("$")) {
                try {
                    if (args.length >= 2)
                        ramaddr = Long.decode(args[1]);
                    else
                        ramaddr = 0;
                    
                    ram = (Ram)Memory.getMemory(args[0].substring(1), Ram.class);
                } catch (NumberFormatException | IOException e) {
                    return error(e.getMessage());
                }
            } else return error("Invalid argument: " + args[0]);
        }
                
        register = new boolean[outputlen];
        clearRegister = new boolean[outputlen];
        
        if (activator!=null) clearOutputs();
        
        if (ram != null) {
            ramListener = new dregisterRamListener();
            ram.addListener(ramListener);
            this.writeBits(ram.read(ramaddr));
            info("Created "+outputlen+"-bit register backed by memory: "+ram.getId()+"@"+Long.toHexString(ramaddr));
        } else
            info("Created "+outputlen+"-bit register.");
        
        return this;
    }
    @Override
    public void shutdown() {
        if (ram != null)
           ram.getListeners().remove(ramListener);
    }
    @Override
    public boolean isStateless() {
        return false;
    }
}
