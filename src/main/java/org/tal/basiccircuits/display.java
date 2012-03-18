package org.tal.basiccircuits;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.eisental.common.parsing.ParsingUtils;
import org.bukkit.DyeColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.tal.basiccircuits.screen.Screen;
import org.tal.redstonechips.bitset.BitSet7;
import org.tal.redstonechips.bitset.BitSetUtils;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.memory.Memory;
import org.tal.redstonechips.memory.Ram;
import org.tal.redstonechips.memory.RamListener;
import org.tal.redstonechips.wireless.Receiver;

/**
 *
 * @author Tal Eisenberg
 */
public class display extends Circuit {
    private Screen screen;
    private int xWordlength, yWordlength, colorWordlength;
    
    private Receiver receiver;
    
    private Ram ram;
    
    private int ramPage = 0, ramPageLength;
    
    @Override
    public void inputChange(int inIdx, boolean state) {
        if (inputBits.get(0)) {
            int x = BitSetUtils.bitSetToUnsignedInt(inputBits, 1, xWordlength);
            int y = BitSetUtils.bitSetToUnsignedInt(inputBits, 1+xWordlength, yWordlength);
            int data = BitSetUtils.bitSetToUnsignedInt(inputBits, 1+xWordlength+yWordlength, colorWordlength);

            try {
                screen.setPixel(x,y,data, true);
            } catch (IllegalArgumentException ie) {
                if (hasListeners()) debug(ie.getMessage());
            }
            
            if (hasListeners()) debug("Setting (" + x + ", " + y + ") to " + data);
        }
    }

    class DisplayReceiver extends Receiver {

        @Override
        public void receive(BitSet7 bits) {
            int x = BitSetUtils.bitSetToUnsignedInt(bits, 0, xWordlength);
            int y = BitSetUtils.bitSetToUnsignedInt(bits, xWordlength, yWordlength);
            int data = BitSetUtils.bitSetToUnsignedInt(bits, xWordlength+yWordlength, colorWordlength);

            try {
                screen.setPixel(x, y, data, true);
            } catch (IllegalArgumentException ie) {
                if (hasListeners()) debug(ie.getMessage());
            }

            if (hasListeners()) debug("Setting (" + x + ", " + y + ") to " + data);
        }        
    }

    class DisplayRamListener implements RamListener {
        @Override
        public void dataChanged(Ram ram, BitSet7 address, BitSet7 data) {
            int color = BitSetUtils.bitSetToUnsignedInt(data, 0, screen.getColorLength());
            int intaddr = BitSetUtils.bitSetToUnsignedInt(address, 0, 32);
            int offset = ramPage * ramPageLength;
            
            if (intaddr >= offset && intaddr < offset + ramPageLength) {
                int idx = intaddr - offset;
                int x = idx % screen.getDescription().addrWidth;
                int y = idx / screen.getDescription().addrWidth;

                try {
                    screen.setPixel(x, y, color, true);
                } catch (IllegalArgumentException ie) {
                    if (hasListeners()) debug(ie.getMessage());
                }

                if (hasListeners()) debug("Setting (" + x + ", " + y + ") to " + data);
            }
        }
    }
    
    private void refreshDisplayFromRam() {
        int offset = ramPage * ramPageLength;
        for (int i=offset; i<offset+ramPageLength; i++) {
            int color = BitSetUtils.bitSetToUnsignedInt(ram.read(i), 0, screen.getColorLength());
            int x = i % screen.getDescription().addrWidth;
            int y = i / screen.getDescription().addrWidth;
            
            try {
                screen.setPixel(x, y, color, true);
            } catch (IllegalArgumentException ie) {
                if (hasListeners()) debug(ie.getMessage());
            }

            if (hasListeners()) debug("Setting (" + x + ", " + y + ") to " + color);
        }
    }
    
    @Override
    protected boolean init(CommandSender sender, String[] args) {
        String channel = null;
        int[] size = null;
        byte[] colorIndex = null;
        
        String[] split = args[0].split("x");
        if (split.length==2 && ParsingUtils.isInt((split[0])) && ParsingUtils.isInt((split[1]))) {
            size = new int[] { Integer.parseInt(split[0]), Integer.parseInt(split[1]) };
        }        
        
        int start = (size==null?0:1);
        if (args.length>start) { // color index
            
            List<Byte> colorList = new ArrayList<Byte>();
            
            for (int i=start; i<args.length; i++) {
                try {
                    colorList.add(DyeColor.valueOf(args[i].toUpperCase()).getData());
                } catch (IllegalArgumentException ie) {
                    // not dye color
                    try {
                        int val = Integer.decode(args[i]);
                        colorList.add((byte)val);
                    } catch (NumberFormatException ne) {
                        if (args[i].startsWith("$")) {
                            try {
                                ram = (Ram)Memory.getMemory(args[i].substring(1), Ram.class);
                            } catch (IllegalArgumentException e) {
                                error(sender, e.getMessage());
                            } catch (IOException e) {
                                error(sender, e.getMessage());
                            }
                        } else if (channel==null) {
                            if (args[i].startsWith("#"))
                                channel = args[i].substring(1);
                            else channel = args[i];
                        } else error(sender, "Invalid argument: " + args[i]);
                    }
                }
            }

            if (!colorList.isEmpty()) {
                colorIndex = new byte[colorList.size()];
                for (int i=0; i<colorList.size(); i++)
                    colorIndex[i] = colorList.get(i);
            }
        }
        
        if (interfaceBlocks.length!=2) {
            error(sender, "Expecting 2 interface blocks. One block in each of 2 opposite corners of the display.");
            return false;
        }
        
        try {
            if (size!=null)
                screen = Screen.generateScreen(interfaceBlocks[0].getLocation(), interfaceBlocks[1].getLocation(),
                        size[0], size[1]);
            else 
                screen = Screen.generateScreen(interfaceBlocks[0].getLocation(), interfaceBlocks[1].getLocation());

            screen.setColorIndex(colorIndex);
            
            if (ram!=null) ramPageLength = screen.getDescription().addrWidth * screen.getDescription().addrHeight;
            
            info(sender, "Successfully scanned display. ");
            info(sender, "The screen is " + 
                    Math.abs(screen.getDescription().physicalWidth) + "m wide, " + 
                    Math.abs(screen.getDescription().physicalHeight) + "m high. Each pixel is " + 
                    Math.abs(screen.getDescription().pixelWidth) + "m on " + 
                    Math.abs(screen.getDescription().pixelHeight) + "m.");            
        } catch (IllegalArgumentException ie) {
            error(sender, ie.getMessage());
            return false;
        }
        
        // expecting 1 clock, enough pins for address width, enough pins for address height, enough pins for color data.
        xWordlength = screen.getXLength(); 
        yWordlength = screen.getYLength(); 
        colorWordlength = screen.getColorLength();

        if (channel==null && ram==null) {
            int expectedInputs = 1 + xWordlength + yWordlength + colorWordlength;
            if (inputs.length!=expectedInputs && (inputs.length!=0 || channel==null)) {
                error(sender, "Expecting " + expectedInputs + " inputs. 1 clock input, " + xWordlength + " x address input(s)" + (yWordlength!=0?", " + yWordlength + "y address input(s)":"") + 
                        ", and " + colorWordlength + " color data inputs.");
                return false;
            } 

            if (sender instanceof Player) {
                info(sender, "inputs: clock - 0, x: 1-" + xWordlength + (yWordlength!=0?", y: " + (xWordlength+1) + "-" + 
                        (xWordlength+yWordlength):"") + ", color: " + (xWordlength+yWordlength+1) + "-" + 
                        (xWordlength+yWordlength+colorWordlength) + ".");
            }
        } else if (channel!=null) {
            try {
                int len = xWordlength+yWordlength+colorWordlength;
                receiver = new DisplayReceiver();
                receiver.init(sender, channel, len, this);
            } catch (IllegalArgumentException ie) {
                error(sender, ie.getMessage());
                return false;
            }
        } 

        if (sender instanceof Player) screen.clear();
        
        if (ram!=null) refreshDisplayFromRam();
        
        return true;
    }
}
