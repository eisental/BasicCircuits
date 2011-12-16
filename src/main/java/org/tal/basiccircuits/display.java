package org.tal.basiccircuits;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.tal.redstonechips.channel.ReceivingCircuit;
import org.tal.redstonechips.util.BitSet7;
import org.tal.redstonechips.util.BitSetUtils;

/**
 *
 * @author Tal Eisenberg
 */
public class display extends ReceivingCircuit {

    private enum Axis {X,Y,Z};

    private byte[] colorIndex;
    private boolean indexedColor = false;
    private int width, height;
    private int pixelWidth, pixelHeight;
    private Location[][][] pixels;
    private int xWordlength, yWordlength, colorWordlength;
    private byte[][] memory;

    @Override
    public void inputChange(int inIdx, boolean state) {
        if (inputBits.get(0)) {
            int x = BitSetUtils.bitSetToUnsignedInt(inputBits, 1, xWordlength);
            int y = BitSetUtils.bitSetToUnsignedInt(inputBits, 1+xWordlength, yWordlength);
            int data = BitSetUtils.bitSetToUnsignedInt(inputBits, 1+xWordlength+yWordlength, colorWordlength);

            setPixel(x,y,data);
        }
    }

    @Override
    protected boolean init(CommandSender sender, String[] args) {
        if (args.length==0) {
            error(sender, "Expecting a display size sign argument");
            return false;
        }

        String[] split = args[0].split("x");
        if (split.length!=2) {
            error(sender, "Bad display argument syntax: " + args[0] + ". Expecting <width>x<height>");
            return false;
        }

        try {
            width = Integer.decode(split[0]);
            height = Integer.decode(split[1]);
        } catch (NumberFormatException ne) {
            error(sender, "Bad display argument syntax: " + args[0] + ". Expecting <width>x<height>");
            return false;
        }

        String channelString = null;
        if (args.length>1) { // color index

            List<Byte> colorList = new ArrayList<Byte>();
            for (int i=1; i<args.length; i++) {
                try {
                    colorList.add(DyeColor.valueOf(args[i].toUpperCase()).getData());
                } catch (IllegalArgumentException ie) {
                    // not dye color
                    try {
                        int val = Integer.decode(args[i]);
                        colorList.add((byte)val);
                    } catch (NumberFormatException ne) {
                        // not dye number also, treat as broadcast channel if last.
                        if (i==args.length-1) {
                            channelString = args[i];
                        } else {
                            error(sender, "Unknown color name: " + args[i]);
                            return false;
                        }
                    }
                }
            }

            if (!colorList.isEmpty()) {
                colorIndex = new byte[colorList.size()];
                for (int i=0; i<colorList.size(); i++)
                    colorIndex[i] = colorList.get(i);
                indexedColor = true;
            }
        }

        // expecting 1 clock, enough pins for address width, enough pins for address height, enough pins for color data.
        xWordlength = calculateRequiredBits(width);
        yWordlength = calculateRequiredBits(height);
        colorWordlength = calculateBpp();

        int expectedInputs = 1 + xWordlength + yWordlength + colorWordlength;
        if (inputs.length!=expectedInputs && (inputs.length!=0 || channelString==null)) {
            error(sender, "Expecting " + expectedInputs + " inputs. 1 clock input, " + xWordlength + " x address input(s) and " + yWordlength + " y address input(s), and " + colorWordlength + " color data inputs.");
            return false;
        }

        if (interfaceBlocks.length!=2) {
            error(sender, "Expecting 2 interface blocks. One block in each of 2 opposite corners of the display.");
            return false;
        }

        try {
            detectDisplay(sender);
        } catch (IllegalArgumentException ie) {
            error(sender, ie.getMessage());
            return false;
        }

        if (channelString!=null) {
            try {
                initWireless(sender, channelString);
            } catch (IllegalArgumentException ie) {
                error(sender, ie.getMessage());
                return false;
            }
        }

        if (sender instanceof Player) clearDisplay();
        return true;
    }

    private void detectDisplay(CommandSender sender) throws IllegalArgumentException {
        int x1 = interfaceBlocks[0].getLocation().getBlockX();
        int x2 = interfaceBlocks[1].getLocation().getBlockX();
        int y1 = interfaceBlocks[0].getLocation().getBlockY();
        int y2 = interfaceBlocks[1].getLocation().getBlockY();
        int z1 = interfaceBlocks[0].getLocation().getBlockZ();
        int z2 = interfaceBlocks[1].getLocation().getBlockZ();

        int dx = Math.abs(x2-x1);
        int dy = Math.abs(y2-y1);
        int dz = Math.abs(z2-z1);
        int xsign = (x2-x1>0?1:-1);
        int ysign = (y2-y1>0?1:-1);
        int zsign = (z2-z1>0?1:-1);

        int phyWidth, phyHeight;

        Axis widthAxis, heightAxis;
        Location origin;

        if (dx==0) { // zy plane
            phyWidth = (dz+1)*zsign;
            phyHeight = (dy+1)*ysign;
            widthAxis = Axis.Z;
            heightAxis = Axis.Y;

            if (world.getBlockTypeIdAt(x1+1, y1, z1)==Material.WOOL.getId())
                origin = new Location(world, x1+1, y1, z1);
            else if (world.getBlockTypeIdAt(x1-1, y1, z1)==Material.WOOL.getId())
                origin = new Location(world, x1-1, y1, z1);
            else throw new IllegalArgumentException("Can't find origin wool block.");

        } else if (dy==0) { // xz plane
            if (dx>=dz) {
                phyWidth = (dx+1)*xsign;
                phyHeight = (dz+1)*zsign;
                widthAxis = Axis.X;
                heightAxis = Axis.Z;
            } else {
                phyWidth = (dz+1)*zsign;
                phyHeight = (dx+1)*xsign;
                widthAxis = Axis.Z;
                heightAxis = Axis.X;
            }

            if (world.getBlockTypeIdAt(x1, y1+1, z1)==Material.WOOL.getId())
                origin = new Location(world, x1, y1+1, z1);
            else if (world.getBlockTypeIdAt(x1, y1-1, z1)==Material.WOOL.getId())
                origin = new Location(world, x1, y1-1, z1);
            else throw new IllegalArgumentException("Can't find origin wool block.");
        } else if (dz==0) { // xy plane
            phyWidth = (dx+1)*xsign;
            phyHeight = (dy+1)*ysign;
            widthAxis = Axis.X;
            heightAxis = Axis.Y;

            if (world.getBlockTypeIdAt(x1, y1, z1+1)==Material.WOOL.getId())
                origin = new Location(world, x1, y1, z1+1);
            else if (world.getBlockTypeIdAt(x1, y1, z1-1)==Material.WOOL.getId())
                origin = new Location(world, x1, y1, z1-1);
            else throw new IllegalArgumentException("Can't find origin wool block.");
        } else throw new IllegalArgumentException("Both interface blocks must be on the same plane.");

        pixelWidth = (int)Math.ceil(phyWidth/width);
        pixelHeight = (int)Math.ceil(phyHeight/height);
        pixels = new Location[width][height][Math.abs(pixelWidth*pixelHeight)];

        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                pixels[x][y] = findPixelBlocks(origin, x, y, widthAxis, heightAxis, pixelWidth, pixelHeight);
                for (Location l : pixels[x][y])
                    if (world.getBlockAt(l).getType()!=Material.WOOL)
                        throw new IllegalArgumentException("Missing wool block at pixel " + x + ", " + y);
            }
        }

        memory = new byte[width][height];

        info(sender, "Successfully scanned display. ");
        info(sender, "The screen is " + Math.abs(phyWidth) + "m wide, " + Math.abs(phyHeight) + "m high. Each pixel is " + Math.abs(pixelWidth) + "m on " + Math.abs(pixelHeight) + "m.");
    }

    @Override
    public void receive(BitSet7 bits) {
        int x = BitSetUtils.bitSetToUnsignedInt(bits, 0, xWordlength);
        int y = BitSetUtils.bitSetToUnsignedInt(bits, xWordlength, yWordlength);
        int data = BitSetUtils.bitSetToUnsignedInt(bits, xWordlength+yWordlength, colorWordlength);

        setPixel(x,y,data);
    }

    @Override
    public int getChannelLength() {
        return xWordlength+yWordlength+colorWordlength;
    }

    private int calculateBpp() {
        if (indexedColor) {
            return calculateRequiredBits(colorIndex.length);
        } else return 4;
    }

    private int calculateRequiredBits(int numOfValues) {
        return (int)Math.ceil(Math.log(numOfValues)/Math.log(2));
    }

    private void setPixel(int x, int y, int data) {
        byte color;
        if (indexedColor) {
            if (data>=colorIndex.length) {
                if (hasDebuggers()) debug("Color index " + data + " is out of bounds.");
                return;
            } else
                color = colorIndex[data];

        } else color = (byte)data;

        if (x>=width || y>=height) {
            if (hasDebuggers()) debug("Pixel (" + x + ", " + y + ") is out of bounds.");
            return;
        }
        
        Location[] pixel = pixels[x][y];

        if (memory[x][y]!=color) {
            if (hasDebuggers()) debug("Setting (" + x + ", " + y + ") to " + DyeColor.getByData((byte)color));

            for (Location l : pixel) {
                l.getBlock().setData(color);
            }
            memory[x][y] = color;
        }
    }

    private Location[] findPixelBlocks(Location origin, int x, int y, Axis widthAxis, Axis heightAxis, int pixelWidth, int pixelHeight) {
        int x1 = origin.getBlockX();
        int y1 = origin.getBlockY();
        int z1 = origin.getBlockZ();

        int dx = x*pixelWidth;
        int dy = y*pixelHeight;

        int xsign = pixelWidth>0?1:-1;
        int ysign = (pixelHeight>0?1:-1);

        pixelWidth = Math.abs(pixelWidth);
        pixelHeight = Math.abs(pixelHeight);

        Location[] ret = new Location[pixelWidth*pixelHeight];

        int i = 0;
        if (widthAxis==Axis.X) {
            if (heightAxis==Axis.Y) {
                for (int ix=0; ix<pixelWidth; ix++) {
                    for (int iy=0; iy<pixelHeight; iy++) {
                        ret[i] = new Location(world, x1+dx+(xsign>0?ix:ix*-1), y1+dy+(ysign>0?iy:iy*-1), z1);
                        i++;
                    }
                }
            } else { // Axis.Z
                for (int ix=0; ix<pixelWidth; ix++) {
                    for (int iy=0; iy<pixelHeight; iy++) {
                        ret[i] = new Location(world, x1+dx+(xsign>0?ix:ix*-1), y1, z1+dy+(ysign>0?iy:iy*-1));
                        i++;
                    }
                }
            }
        } else if (widthAxis==Axis.Y) {
            if (heightAxis==Axis.X) {
                for (int ix=0; ix<pixelWidth; ix++) {
                    for (int iy=0; iy<pixelHeight; iy++) {
                        ret[i] = new Location(world, x1+dy+(ysign>0?iy:iy*-1), y1+dx+(xsign>0?ix:ix*-1), z1);
                        i++;
                    }
                }
            } else {// Axis.Z
                for (int ix=0; ix<pixelWidth; ix++) {
                    for (int iy=0; iy<pixelHeight; iy++) {
                        ret[i] = new Location(world, x1, y1+dx+(xsign>0?ix:ix*-1), z1+dy+(ysign>0?iy:iy*-1));
                        i++;
                    }
                }
            }
        } else { // Axis.Z
            if (heightAxis==Axis.Y) {
                for (int ix=0; ix<pixelWidth; ix++) {
                    for (int iy=0; iy<pixelHeight; iy++) {
                        ret[i] = new Location(world, x1, y1+dy+(ysign>0?iy:iy*-1), z1+dx+(xsign>0?ix:ix*-1));
                        i++;
                    }
                }
            } else { // Axis.X
                for (int ix=0; ix<pixelWidth; ix++) {
                    for (int iy=0; iy<pixelHeight; iy++) {
                        ret[i] = new Location(world, x1+dy+(ysign>0?iy:iy*-1), y1, z1+dx+(xsign>0?ix:ix*-1));
                        i++;
                    }
                }
            }
        }

        return ret;
    }

    public void clearDisplay() {
        for (int y=0; y<height; y++) {
            for (int x=0; x<width; x++) {
                this.setPixel(x, y, 0);
            }
        }
    }
}
