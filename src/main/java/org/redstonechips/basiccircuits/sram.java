
package org.redstonechips.basiccircuits;

import java.io.IOException;
import java.util.logging.Level;
import org.redstonechips.paging.LineSource;
import org.redstonechips.parsing.Range;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.redstonechips.RCPrefs;
import org.redstonechips.RCTypeReceiver;
import org.redstonechips.circuit.Circuit;
import org.redstonechips.memory.Memory;
import org.redstonechips.memory.Ram;
import org.redstonechips.memory.RamListener;
import org.redstonechips.paging.Pager;
import org.redstonechips.util.BooleanArrays;

/**
 *
 * @author Tal Eisenberg
 */
public class sram extends Circuit implements RCTypeReceiver, RamListener {

    Ram memory;
    int addressLength;
    int wordLength;
    int readWritePin;
    int disablePin;
    int addressPin;
    int dataPin;
    
    boolean readOnlyMode;

    boolean sramDisable = false;
    boolean readWrite = false;

    boolean anonymous = true;
    
    @Override
    public void input(boolean state, int inIdx) {
        if (inIdx==readWritePin) {
            readWrite = state;
            boolean[] data = getInputData();

            if (readWrite && !sramDisable) { // store current data inputs when readWrite goes high.
                long address = getInputAddress();
                if (chip.hasListeners()) debug("Writing " + BooleanArrays.toPrettyString(data, wordLength) + " to address " + address);
                memory.write(address, data);
            } else { // write data to outputs.
                this.writeBits(inputs, dataPin, wordLength);
            }
        } else if (inIdx==disablePin) {
            sramDisable = state;
            if (chip.hasListeners()) debug("Chip " + (sramDisable?"disabled.":"enabled"));
            if (sramDisable) {
                clearOutputs();
            } else {
                if (readWrite) readMemory();
                else writeBits(getInputData());
            }

            writeBitSet(BooleanArrays.toBitSet(outputs));
        } else if (inIdx>=addressPin && inIdx<addressPin+addressLength) {
            if (readWrite && !sramDisable) {
                readMemory();
            }
        } else if (inIdx>=dataPin && inIdx<dataPin+wordLength) {
            if (!readWrite && !sramDisable) {
                // copy data inputs to outputs
                writeBits(getInputData());
            }
        }
    }

    @Override
    public Circuit init(String[] args) {
        wordLength = outputlen;

        if (args.length>1 && (args[1].equalsIgnoreCase("readonly") || args[1].equalsIgnoreCase("rom"))) {
            readOnlyMode = true;
            addressLength = inputlen-1;
            addressPin = 1;
            disablePin = 0;
            dataPin = -1;
            readWritePin = -1;
            readWrite = true;
        } else {
            readOnlyMode = false;
            addressLength = inputlen-2-wordLength;
            addressPin = 2;
            disablePin = 1;
            readWritePin = 0;
            dataPin = addressPin + addressLength;
        }

        if (outputlen==0) return error("Expecting at least 1 output pin.");

        if (addressLength<1) {
            if (readOnlyMode) return error("Expecting at least 1 control pin, and 1 address input pin.");                
            else return error("Expecting at least 2 control pins, 1 address input pin, and " + wordLength + " data pins.");
        }
        
        try {
            if (args.length>0) {
                anonymous = false;
                // if new memory subclasses are added there should be a check here for memory class.
                memory = (Ram)Memory.getMemory(args[0], Ram.class);
            } else {
                anonymous = true;
                memory = (Ram)Memory.getAnonymousMemory(Ram.class);
            }
        } catch (IOException ex) {
            return error("While creating new memory file: " + ex);
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        }
        
        if (!readOnlyMode) {
            readWrite = inputs[readWritePin];
        }
        
        sramDisable = inputs[disablePin];
        
        rc.addRCTypeReceiver(chip.activationBlock, this);
        if (activator!=null) clearOutputs();
        if (readWrite && !sramDisable) {
            readMemory();
        }
        
        memory.addListener(this);
        info("This sram chip can hold up to " + Math.pow(2, addressLength) + "x" + wordLength + " bits. Memory data will be stored at " + ChatColor.YELLOW + memory.getFile().getPath());
        
        return this;
    }

    @Override
    public void destroyed() {
        if (anonymous) {
            if (!memory.delete()) {
                rc.log(Level.SEVERE, "Could not delete memory file: " + memory.getFile());
            }
        }
    }

    @Override
    public void save() {
        try {
            memory.store();
        } catch (IOException ex) {
            rc.log(Level.SEVERE, "While saving memory to file: " + ex.getMessage());
        }
    }

    private void readMemory() {
        long address = getInputAddress();
        boolean[] data = memory.read(address, wordLength);
        if (chip.hasListeners()) debug("Reading " + BooleanArrays.toPrettyString(data, wordLength) + " from address " + address);
        writeBits(data, 0, wordLength);
    }
    
    @Override
    public void type(String[] words, Player player) {
        if (words.length==0) return;
        int curIdx = 0;

        if (words[0].equalsIgnoreCase("ascii")) { // ascii string
            StringBuilder b = new StringBuilder();
            for (int i=1; i<words.length; i++)
               b.append(words[i]);

            String ascii = b.toString();
            for (int i=0; i<ascii.length(); i++)
                memory.write(i, BooleanArrays.fromInt(ascii.charAt(i), wordLength));
            
        } else if (words[0].equalsIgnoreCase("notes")) { // notes notation (unsupported).
            
        } else if (words[0].equalsIgnoreCase("dump")) { // print memory contents.
            if (words.length==1) {
                dumpMemory(player, null);
            } else {
                try {
                    dumpMemory(player, words[1]);
                } catch (IllegalArgumentException ie) {
                    player.sendMessage("Bad range argument: " + words[1]);
                }
            }

        } else {
            for (String word : words) {
                // either idx:value or just value
                int colonIdx = word.indexOf(":");
                try {
                    if (colonIdx==-1) {
                        // use running index
                        boolean[] value = parseData(player, word);
                        if (value==null) return;
                        memory.write(curIdx, value);
                        curIdx++;
                    } else {
                        int address = Integer.decode(word.substring(0, colonIdx));
                        boolean[] value = parseData(player, word.substring(colonIdx+1));
                        if (value==null) return;
                        memory.write(address, value);
                    }
                } catch (NumberFormatException ne) {
                    errorForSender(player, "Bad entry. Expecting either a value or <address>:<value> - " + word);
                    return;
                }
            }
            infoForSender(player, "Successfully written to memory.");
        }
    }

    private boolean[] parseData(CommandSender sender, String data) {
        try {
            int ret = Integer.decode(data);
            return BooleanArrays.fromInt(ret, wordLength);
        } catch (NumberFormatException ne) {
            if (data.length()==1) return BooleanArrays.fromInt(data.charAt(0), wordLength); // ascii character
            else if (data.startsWith("b")) { // binary notation (b******)
                String bits = data.substring(1);
                return BooleanArrays.fromString(bits);
            } else {
                errorForSender(sender, "Bad data: " + data + ". Expecting either a number or 1 ascii character.");
                return null;
            }
        }
    }

    private void dumpMemory(Player player, String srange) {
        int firstAddress, lastAddress;

        if (srange==null) {
            firstAddress = 0;
            lastAddress = (int)Math.pow(2, addressLength)-1;
        } else {
            Range range = new Range(srange, Range.Type.OPEN_ALLOWED);
            firstAddress = (int)(range.hasLowerLimit()?range.getOrderedRange()[0]:0);
            lastAddress = (int)(range.hasUpperLimit()?range.getOrderedRange()[1]:Math.pow(2, addressLength));
        }

        if (firstAddress>=0 && lastAddress>=0) {
            String titleRange;
            if (firstAddress==lastAddress)
                titleRange = Integer.toString(firstAddress);
            else titleRange = firstAddress + "-" + lastAddress;
            
            MemoryLineSource l = new MemoryLineSource(firstAddress, lastAddress-firstAddress+1);
            
            Pager.beginPaging(player, "sram " + memory.getId() + " memory (" + titleRange + ")", 
                    l, RCPrefs.getInfoColor(), RCPrefs.getErrorColor());
        } else 
            errorForSender(player, "Invalid address range: " + firstAddress + ".." + lastAddress);
    }
            
    @Override
    public boolean isStateless() {
        return false;
    }

    @Override
    public void dataChanged(Ram ram, long address, boolean[] data) {
        if (sramDisable) return;
                
        long curaddr = getInputAddress();
        if (readWrite && curaddr == address) readMemory();
    }

    @Override
    public void shutdown() {
        memory.getListeners().remove(this);
	memory.release();
    }
    

    
    private long getInputAddress() {
        boolean[] address = new boolean[addressLength];
        System.arraycopy(inputs, addressPin, address, 0, addressLength);
        return BooleanArrays.toUnsignedInt(inputs, addressPin, addressLength);
    }
    
    private boolean[] getInputData() {
        boolean[] data = new boolean[wordLength];
        System.arraycopy(inputs, dataPin, data, 0, wordLength);
        return data;
    }
    
    class MemoryLineSource implements LineSource {
        int offset;
        int length;
        
        public MemoryLineSource(int offset, int length) {
            this.offset = offset;
            this.length = length;
        }
        
        @Override
        public String getLine(int idx) {
            String value;
            String address = zeroPad(idx+offset, (int)Math.pow(2, addressLength)-1);
            boolean[] data = memory.read(idx+offset);
            if (wordLength>32) value = Long.toHexString(BooleanArrays.toSignedInt(data, 0, wordLength));
            else value = BooleanArrays.toPrettyString(data, wordLength);
            return ChatColor.YELLOW.toString() + address + ": " + ChatColor.WHITE + value + "\n";
        }

        @Override
        public int getLineCount() {
            return length;
        }
        
        private String zeroPad(int a, int max) {
            String pad = "";
            String address = Integer.toString(a);
            int charCount = Integer.toString(max).length();
            for (int i=0; i<charCount-address.length(); i++) pad += "0";
            return pad + address;
        }
    }
}
