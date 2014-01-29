package org.redstonechips.basiccircuits;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.redstonechips.parsing.Parsing;
import org.bukkit.DyeColor;
import org.redstonechips.basiccircuits.screen.Screen;
import org.redstonechips.chip.Circuit;
import org.redstonechips.memory.Memory;
import org.redstonechips.memory.Ram;
import org.redstonechips.memory.RamListener;
import org.redstonechips.util.BooleanArrays;
import org.redstonechips.util.BooleanSubset;
import org.redstonechips.wireless.Receiver;

/**
 *
 * @author Tal Eisenberg
 */
public class display extends Circuit {
    private Screen screen;
    private int xWordlength, yWordlength, colorWordlength;
    
    private Receiver receiver;
    
    private Ram ram;
    private RamListener ramListener;
    
    private long ramPage = 0;
    private int ramPageLength;
    
    @Override
    public void input(boolean state, int inIdx) {
        if (!inputs[0]) return;
        
        if (ram==null) {
            // set pixel
            processPixelInput(inputs, 1);
        } else {
            // update ram page
            ramPage = BooleanArrays.toUnsignedInt(inputs, 1, inputlen-1);
            if (chip.hasListeners()) debug("Moving to ram page " + ramPage);
            refreshDisplayFromRam();
        }
    }

    class DisplayReceiver extends Receiver {

        @Override
        public void receive(BooleanSubset bits) {
            int x = (int)bits.toUnsignedInt(0, xWordlength);
            int y = (int)bits.toUnsignedInt(xWordlength, yWordlength);
            int color = (int)bits.toUnsignedInt(xWordlength+yWordlength, colorWordlength);
            processPixelInput(x, y, color); // set pixel
        }        
    }

    private void processPixelInput(int x, int y, int color) {
        try {
            screen.setPixel(x, y, color, true);
        } catch (IllegalArgumentException ie) {
            if (chip.hasListeners()) debug(ie.getMessage());
        }

        if (chip.hasListeners()) debug("Setting (" + x + ", " + y + ") to " + color);                
    }
    
    private void processPixelInput(boolean[] bits, int startIdx) {
        int x = (int)BooleanArrays.toUnsignedInt(bits, startIdx, xWordlength);
        int y = (int)BooleanArrays.toUnsignedInt(bits, startIdx+xWordlength, yWordlength);
        int color = (int)BooleanArrays.toUnsignedInt(bits, startIdx+xWordlength+yWordlength, colorWordlength);

        processPixelInput(x, y, color);
    }
    
    class DisplayRamListener implements RamListener {
        @Override
        public void dataChanged(Ram ram, long address, boolean[] data) {
            int color = (int)BooleanArrays.toUnsignedInt(data);
            long offset = ramPage * ramPageLength;
            
            if (address >= offset && address < offset + ramPageLength) {
                long idx = address - offset;
                int x = (int)(idx % screen.getDescription().addrWidth);
                int y = (int)(idx / screen.getDescription().addrWidth);

                try {
                    screen.setPixel(x, y, color, true);
                } catch (IllegalArgumentException ie) {
                    if (chip.hasListeners()) debug(ie.getMessage());
                }

                if (chip.hasListeners()) debug("Setting (" + x + ", " + y + ") to " + data);
            }
        }
    }
    
    private void refreshDisplayFromRam() {
        long offset = ramPage * ramPageLength;
        for (long i=offset; i<offset+ramPageLength; i++) {
            int color = (int)BooleanArrays.toUnsignedInt(ram.read(i));
            int x = (int)((i-offset) % screen.getDescription().addrWidth);
            int y = (int)((i-offset) / screen.getDescription().addrWidth);
            
            try {
                screen.setPixel(x, y, color, true);
            } catch (IllegalArgumentException ie) {
                if (chip.hasListeners()) debug(ie.getMessage());
            }

            if (chip.hasListeners()) debug("Setting (" + x + ", " + y + ") to " + color);
        }
    }
    
    @Override
    public Circuit init(String[] args) {
        String channel = null;
        int[] size = null;
        byte[] colorIndex = null;
        
        if (args.length>0) {
            String[] split = args[0].split("x");
            if (split.length==2 && Parsing.isInt(split[0]) && Parsing.isInt(split[1])) {
                size = new int[] { Integer.parseInt(split[0]), Integer.parseInt(split[1]) };
            }                    
        }
        
        
        int start = (size==null?0:1);
        if (args.length>start) { // color index
            
            List<Byte> colorList = new ArrayList<>();
            
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
                            } catch (IllegalArgumentException | IOException e) {
                                return error(e.getMessage());
                            }
                        } else if (channel==null) {
                            if (args[i].startsWith("#"))
                                channel = args[i].substring(1);
                            else channel = args[i];
                        } else {
                            return error("Invalid argument: " + args[i]);
                        }
                    }
                }
            }

            if (!colorList.isEmpty()) {
                colorIndex = new byte[colorList.size()];
                for (int i=0; i<colorList.size(); i++)
                    colorIndex[i] = colorList.get(i);
            }
        }
        
        if (chip.interfaceBlocks.length!=2)
            return error("Expecting 2 interface blocks. One block in each of 2 opposite corners of the display.");
        
        try {
            if (size!=null)
                screen = Screen.generateScreen(chip.interfaceBlocks[0].getLocation(), chip.interfaceBlocks[1].getLocation(),
                        size[0], size[1]);
            else 
                screen = Screen.generateScreen(chip.interfaceBlocks[0].getLocation(), chip.interfaceBlocks[1].getLocation());

            screen.setColorIndex(colorIndex);
            
            if (ram!=null) ramPageLength = screen.getDescription().addrWidth * screen.getDescription().addrHeight;
            
            info("Successfully scanned display. ");
            info("The screen is " + 
                    Math.abs(screen.getDescription().physicalWidth) + "m wide, " + 
                    Math.abs(screen.getDescription().physicalHeight) + "m high. Each pixel is " + 
                    Math.abs(screen.getDescription().pixelWidth) + "m on " + 
                    Math.abs(screen.getDescription().pixelHeight) + "m.");            
            
            if (ram!=null) info("Reading pixel data from memory: " + ram.getId());
        } catch (IllegalArgumentException ie) {
            return error(ie.getMessage());
        }
        
        // expecting 1 clock, enough pins for address width, enough pins for address height, enough pins for color data.
        xWordlength = screen.getXLength(); 
        yWordlength = screen.getYLength(); 
        colorWordlength = screen.getColorLength();

        if (channel==null && ram==null) {
            int expectedInputs = 1 + xWordlength + yWordlength + colorWordlength;
            if (inputlen!=expectedInputs && (inputlen!=0 || channel==null)) {
                return error("Expecting " + expectedInputs + " inputs. 1 clock input, " + xWordlength + " x address input(s)" + (yWordlength!=0?", " + yWordlength + " y address input(s)":"") + 
                        ", and " + colorWordlength + " color data inputs.");
            } 

            if (activator != null) {
                info("inputs: clock - 0, x: 1-" + xWordlength + (yWordlength!=0?", y: " + (xWordlength+1) + "-" + 
                        (xWordlength+yWordlength):"") + ", color: " + (xWordlength+yWordlength+1) + "-" + 
                        (xWordlength+yWordlength+colorWordlength) + ".");
            }
        } else if (channel!=null) {
            try {
                int len = xWordlength+yWordlength+colorWordlength;
                receiver = new DisplayReceiver();
                receiver.init(activator, channel, len, this);
            } catch (IllegalArgumentException ie) {
                return error(ie.getMessage());
            }
        } else if (ram!=null) {
            ramListener = new DisplayRamListener();
            ram.addListener(ramListener);
        }

        if (activator!=null) screen.clear();
        
        if (ram!=null) refreshDisplayFromRam();
        
        return this;
    }
    @Override
    public void shutdown() {
        if (ram != null) {
           ram.getListeners().remove(ramListener);
           ram.release();
        }        
    }
}
