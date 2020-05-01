package org.redstonechips.basiccircuits;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.redstonechips.RCPrefs;
import org.redstonechips.circuit.Circuit;
import org.redstonechips.chip.io.InterfaceBlock;
import org.redstonechips.util.BooleanArrays;
import org.redstonechips.util.BooleanSubset;
import org.redstonechips.util.Locations;
import org.redstonechips.wireless.Receiver;


public class pixel extends Circuit {
	
    	
	public byte ColorToNum(String colornametext) {
		switch(colornametext)
		{
		case "WHITE":
			return(0);
		case "ORANGE":
			return(1);
		case "MAGENTA":
			return(2);
		case "LIGHT_BLUE":
			return(3);
		case "YELLOW":
			return(4);
		case "LIME":
			return(5);
		case "PINK":
			return(6);
		case "GRAY":
			return(7);
		case "LIGHT_GRAY":
			return(8);
		case "CYAN":
			return(9);
		case "PURPLE":
			return(10);
		case "BLUE":
			return(11);
		case "BROWN":
			return(12);
		case "GREEN":
			return(13);
		case "RED":
			return(14);
		case "BLACK":
			return(15);
		}		
		return 16;
	}
	public Material ColorSel(byte colornum) {  
    	switch(colornum)
    	{
    	case 0 :
    		return(Material.WHITE_WOOL);
    	case 1 :
    		return(Material.ORANGE_WOOL);
    	case 2 :
    		return(Material.MAGENTA_WOOL);
    	case 3 :
    		return(Material.LIGHT_BLUE_WOOL);
    	case 4 :
    		return(Material.YELLOW_WOOL);
    	case 5 :
    		return(Material.LIME_WOOL);
    	case 6 :
    		return(Material.PINK_WOOL);
    	case 7 :
    		return(Material.GRAY_WOOL);
    	case 8 :
    		return(Material.LIGHT_GRAY_WOOL);
    	case 9 :
    		return(Material.CYAN_WOOL);
    	case 10 :
    		return(Material.PURPLE_WOOL);
    	case 11 :
    		return(Material.BLUE_WOOL);
    	case 12 :
    		return(Material.BROWN_WOOL);
    	case 13 :
    		return(Material.GREEN_WOOL);
    	case 14 :
    		return(Material.RED_WOOL);
    	case 15 :
    		return(Material.BLACK_WOOL);  	
    	}	
    
    	return(Material.WHITE_WOOL);
    }
	
	public String ColortoString(byte colornum) {  
    	switch(colornum)
    	{
    	case 0 :
    		return("White");
    	case 1 :
    		return("Orange");
    	case 2 :
    		return("Magenta");
    	case 3 :
    		return("Light Blue");
    	case 4 :
    		return("Yellow");
    	case 5 :
    		return("Lime");
    	case 6 :
    		return("Pink");
    	case 7 :
    		return("Gray");
    	case 8 :
    		return("Light Gray");
    	case 9 :
    		return("Cyan");
    	case 10 :
    		return("Purple");
    	case 11 :
    		return("Blue");
    	case 12 :
    		return("Brown");
    	case 13 :
    		return("Green");
    	case 14 :
    		return("Red");
    	case 15 :
    		return("Black");  	
    	}    
    	return("White");
    }
	
	private boolean indexedColor = false;
    private byte[] colorIndex;
    private int distance = 3;
    private static final BlockFace[] faces = new BlockFace[] { BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };
    
    private Location[] colorBlocks;
    
    private Receiver receiver;
            
    @Override
    public void input(boolean state, int inIdx) {
        if (inputlen==1) {
            updatePixel(inputs);
        } else if (inIdx==0 && state) { // clock pin
            updatePixel(inputs);
        }
    }

    @Override
    public Circuit init(String[] args) {
        if (inputlen>5) return error("Too many inputs. Expecting 1 clock pin and no more than 4 data pins.");
        else if (chip.interfaceBlocks.length==0) return error("Expecting at least 1 interface block.");
        
        if (args.length>0) {
            String channelString = null;

            List<Byte> colorList = new ArrayList<>();
            for (int i=0; i<args.length; i++) {      	
            	if (ColorToNum(args[i].toUpperCase()) != 16) colorList.add(ColorToNum(args[i].toUpperCase())); //check dye color text and add            	
            	else { 	// no color text         			
            		
            	    try {
                        int val = Integer.decode(args[i]);
                        colorList.add((byte)val);
                    } catch (NumberFormatException ne) {
                     // not color number also, treat as broadcast channel if last or distance argument;
                        if ((args[i].startsWith("d{") || args[i].startsWith("dist{")) && args[i].endsWith("}")) {
                            try {
                                distance = Integer.decode(args[i].substring(args[i].indexOf("{")+1, args[i].length()-1));
                                int maxDistance = getMaxDistance();
                                if (maxDistance>=0 && distance>maxDistance)
                                    return error("A distance value of " + distance + " is not allowed. The maximum value is " + maxDistance + ".");
                                
                            } catch (NumberFormatException ne2) {
                                return error("Bad distance argument: " + args[i] + ". Expecting d{<distance>} or dist{<distance>}.");
                            }
                        } else if (i==args.length-1) {
                            channelString = args[i];
                        } else
                            return error("Unknown color name: " + args[i]);
                    }
                }
            
            }

            // color index
            if (!colorList.isEmpty()) {
                colorIndex = new byte[colorList.size()];
                for (int i=0; i<colorList.size(); i++) {
                    colorIndex[i] = colorList.get(i);
                }

                indexedColor = true;
            }

            // wireless broadcast channel
            if (channelString!=null) {
                try {
                    receiver = new PixelReceiver();
                    int len;
                    if (!indexedColor)
                        len = 4;
                    else {
                        len = (int)Math.ceil(Math.log(colorIndex.length)/Math.log(2));
                    }
                    receiver.init(activator, channelString, len, this);
                } catch (IllegalArgumentException ie) {
                    return error(ie.getMessage());
                }
            }
        }

        // find color blocks
        List<Location> blockList = new ArrayList<>();
        for (InterfaceBlock i : chip.interfaceBlocks) {
            findColorBlocksAround(i.getLocation(), i.getLocation(), blockList, distance, 0);
        }
        
        colorBlocks = blockList.toArray(new Location[0]);
        
        // add color blocks to chip structure.
        blockList.addAll(Arrays.asList(chip.structure));
        chip.structure = blockList.toArray(new Location[0]);

        
        
        return this;
    }

    private void updatePixel(boolean[] bits) {
        for (InterfaceBlock i : chip.interfaceBlocks)
            if (!i.getLocation().getChunk().isLoaded()) return;

        int val;
        byte color;
        if (inputlen<=1) val = (int)BooleanArrays.toUnsignedInt(bits);
        else val = (int)BooleanArrays.toUnsignedInt(bits, 1, bits.length-1);

        if (indexedColor) {
            int index = val;
            if (index>=colorIndex.length) {
                if (chip.hasListeners()) debug("Color index out of bounds: " + index);
                return;
            }
            color = colorIndex[index];
        }   else color = (byte)val;
              
        if (chip.hasListeners()) debug("Setting pixel color to " + ColortoString(color));

        for (Location l : colorBlocks) {
        	Block b = l.getBlock();
            BlockState bState = b.getState();
            bState.setType(ColorSel(color));
            bState.update(true, false);        	
        }

    }

    @SuppressWarnings("incomplete-switch")
	private static void findColorBlocksAround(Location origin, Location curLocation, List<Location> coloredBlocks, int range, int curDist) {
        if (curDist>=range) return;

        curDist++;
        for (BlockFace face : faces) {
            Location attached = Locations.getFace(curLocation, face);
            switch (attached.getBlock().getType()) {
            case WHITE_WOOL:
            case ORANGE_WOOL:
            case MAGENTA_WOOL:
            case LIGHT_BLUE_WOOL:
            case YELLOW_WOOL:
            case LIME_WOOL:
            case PINK_WOOL:
            case GRAY_WOOL:
            case LIGHT_GRAY_WOOL:
            case CYAN_WOOL:
            case PURPLE_WOOL:
            case BLUE_WOOL:
            case BROWN_WOOL:
            case GREEN_WOOL:
            case RED_WOOL:
            case BLACK_WOOL:
                    if (!coloredBlocks.contains(attached) && !attached.equals(origin) && inCube(origin, attached, range))
                        coloredBlocks.add(attached);
                    findColorBlocksAround(origin, attached, coloredBlocks, range, curDist);                   
            }
        }
    }

    private static boolean inCube(Location origin, Location f, int rad) {
        int dx = (int)Math.abs(origin.getX()-f.getX());
        int dy = (int)Math.abs(origin.getY()-f.getY());
        int dz = (int)Math.abs(origin.getZ()-f.getZ());

        if (rad>2) rad--;
        else rad++;

        return (dx<rad && dy<rad && dz<rad);
    }

    private int getMaxDistance() {
        Object oMaxDist = RCPrefs.getPref("pixel.maxDistance");
        if (oMaxDist != null && oMaxDist instanceof Integer) return (Integer)oMaxDist;
        else return -1;
    }
    
    class PixelReceiver extends Receiver {
        @Override
        public void receive(BooleanSubset bits) {
            // if we have 0 or 1 inputs there's no clock to adjust. just use the incoming bits.        
            boolean[] valbits;
            if (inputlen<=1) {
                valbits = bits.copy(0, bits.length());
            }  else {
                valbits = new boolean[bits.length()+1];
                for (int i=0; i<bits.length(); i++)
                    valbits[i+1] = bits.get(i);
                valbits[0] = false;
            }
            updatePixel(valbits);
        }        
    }
}
