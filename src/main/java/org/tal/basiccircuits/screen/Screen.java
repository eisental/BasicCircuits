package org.tal.basiccircuits.screen;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

/**
 *
 * @author Tal Eisenberg
 */
public class Screen {

    public int getXLength() {
        return calculateBitLength(ds.addrWidth);
    }
    
    public int getYLength() {
        return calculateBitLength(ds.addrHeight);
    }

    public enum Axis {X,Y,Z};

    private final ScreenDescription ds;
    
    private byte[] colorIndex = null;
    
    private int colorLength = 4;
    
    private Location[][][] pixels;
    private byte[][] memory;

    private Screen(ScreenDescription ds) {
        this.ds = ds;
        
        int pixelWidth = (int)Math.ceil(ds.physicalWidth/ds.addrWidth);
        int pixelHeight = (int)Math.ceil(ds.physicalHeight/ds.addrHeight);
        pixels = new Location[ds.addrWidth][ds.addrHeight][Math.abs(pixelWidth*pixelHeight)];

        for (int x=0; x<ds.addrWidth; x++) {
            for (int y=0; y<ds.addrHeight; y++) {
                pixels[x][y] = findPixelBlocks(ds.origin, x, y, ds.widthAxis, ds.heightAxis, pixelWidth, pixelHeight);
            }
        }
        
        ds.pixelWidth = pixelWidth;
        ds.pixelHeight = pixelHeight;
        memory = new byte[ds.addrWidth][ds.addrHeight];        
    }
    
    public void setColorIndex(byte[] index) { 
        this.colorIndex = index;
        if (colorIndex==null) colorLength = 4;
        else colorLength = calculateBitLength(colorIndex.length);        
    }
    
    public ScreenDescription getDescription() {
        return ds;
    }
    
    public int getColorLength() {
        return colorLength;
    }
    
    public void setPixel(int x, int y, int data, boolean checkMemory) {
        byte color;
        if (colorIndex!=null) {
            if (data>=colorIndex.length) {
                throw new IllegalArgumentException("Color index " + data + " is out of bounds.");
            } else
                color = colorIndex[data];

        } else color = (byte)data;

        if (x>=ds.addrWidth || y>=ds.addrHeight) {
            throw new IllegalArgumentException("Pixel (" + x + ", " + y + ") is out of bounds.");
        }
        
        Location[] pixel = pixels[x][y];

        if (!checkMemory || memory[x][y]!=color) {
            for (Location l : pixel) {
                Block b = l.getBlock();
                if (b.getType()==Material.WOOL)
                    b.setData(color);
            }
            memory[x][y] = color;
        } 
    }
    
    public void clear() {
        for (int y=0; y<ds.addrHeight; y++) {
            for (int x=0; x<ds.addrWidth; x++) {
                this.setPixel(x, y, 0, false);
            }
        }        
    }
    
    public static Screen generateScreen(Location l0, Location l1) {
        ScreenDescription ds = scanScreen(l0, l1);        
        ds.addrWidth = Math.abs(ds.physicalWidth);
        ds.addrHeight = Math.abs(ds.physicalHeight);
                
        return new Screen(ds);        
    }
    
    public static Screen generateScreen(Location l0, Location l1, int addrWidth, int addrHeight) {
        ScreenDescription ds = scanScreen(l0, l1);        
        ds.addrWidth = addrWidth;
        ds.addrHeight = addrHeight;
                
        return new Screen(ds);
    }
    
    public static ScreenDescription scanScreen(Location l0, Location l1) {        
        if (!l0.getWorld().equals(l1.getWorld()))
            throw new IllegalArgumentException("Both screen corners must be on the same world.");
        
        World world = l0.getWorld();
        int x1 = l0.getBlockX();
        int x2 = l1.getBlockX();
        int y1 = l0.getBlockY();
        int y2 = l1.getBlockY();
        int z1 = l0.getBlockZ();
        int z2 = l1.getBlockZ();

        int dx = Math.abs(x2-x1);
        int dy = Math.abs(y2-y1);
        int dz = Math.abs(z2-z1);
        int xsign = (x2-x1>0?1:-1);
        int ysign = (y2-y1>0?1:-1);
        int zsign = (z2-z1>0?1:-1);

        int phyWidth, phyHeight;

        Axis widthAxis, heightAxis;
        Location origin;

        if (dx==0 && dy!=0 && dz!=0) { // zy plane
            phyWidth = (dz+1)*zsign;
            phyHeight = (dy+1)*ysign;
            widthAxis = Axis.Z;
            heightAxis = Axis.Y;

            if (world.getBlockTypeIdAt(x1+1, y1, z1)==Material.WOOL.getId())
                origin = new Location(world, x1+1, y1, z1);
            else if (world.getBlockTypeIdAt(x1-1, y1, z1)==Material.WOOL.getId())
                origin = new Location(world, x1-1, y1, z1);
            else throw new IllegalArgumentException("Can't find origin wool block.");

        } else if (dx!=0 && dy==0 && dz!=0) { // xz plane
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
        } else if (dx!=0 && dy!=0 && dz==0) { // xy plane
            phyWidth = (dx+1)*xsign;
            phyHeight = (dy+1)*ysign;
            widthAxis = Axis.X;
            heightAxis = Axis.Y;

            if (world.getBlockTypeIdAt(x1, y1, z1+1)==Material.WOOL.getId())
                origin = new Location(world, x1, y1, z1+1);
            else if (world.getBlockTypeIdAt(x1, y1, z1-1)==Material.WOOL.getId())
                origin = new Location(world, x1, y1, z1-1);
            else throw new IllegalArgumentException("Can't find origin wool block.");
        } else if (dx==0 && dy!=0 && dz==0) { // y line
            phyWidth = (dy+1)*ysign;
            phyHeight = 1;
            widthAxis = Axis.Y;
            
            if (world.getBlockTypeIdAt(x1+1, y1, z1)==Material.WOOL.getId()) {
                heightAxis = Axis.Z;
                origin = new Location(world, x1+1, y1, z1);
            } else if (world.getBlockTypeIdAt(x1-1, y1, z1)==Material.WOOL.getId()) {
                heightAxis = Axis.Z;
                origin = new Location(world, x1-1, y1, z1);
            } else if (world.getBlockTypeIdAt(x1, y1, z1+1)==Material.WOOL.getId()) {
                heightAxis = Axis.X;
                origin = new Location(world, x1, y1, z1+1);
            } else if (world.getBlockTypeIdAt(x1, y1, z1-1)==Material.WOOL.getId()) {
                heightAxis = Axis.X;
                origin = new Location(world, x1, y1, z1-1);
            } else throw new IllegalArgumentException("Can't find origin wool block.");
        } else if (dx!=0 && dy==0 && dz==0) { // x line
            phyWidth = (dx+1)*xsign;
            phyHeight = 1;
            widthAxis = Axis.X;
            
            if (world.getBlockTypeIdAt(x1, y1+1, z1)==Material.WOOL.getId()) {
                heightAxis = Axis.Z;
                origin = new Location(world, x1, y1+1, z1);
            } else if (world.getBlockTypeIdAt(x1, y1-1, z1)==Material.WOOL.getId()) {
                heightAxis = Axis.Z;
                origin = new Location(world, x1, y1-1, z1);
            } else if (world.getBlockTypeIdAt(x1, y1, z1+1)==Material.WOOL.getId()) {
                heightAxis = Axis.Y;
                origin = new Location(world, x1, y1, z1+1);
            } else if (world.getBlockTypeIdAt(x1, y1, z1-1)==Material.WOOL.getId()) {
                heightAxis = Axis.Y;
                origin = new Location(world, x1, y1, z1-1);
            } else throw new IllegalArgumentException("Can't find origin wool block.");
        } else if (dx==0 && dy==0 && dz!=0) { // z line
            phyWidth = (dz+1)*zsign;
            phyHeight = 1;
            widthAxis = Axis.Z;
            
            if (world.getBlockTypeIdAt(x1, y1+1, z1)==Material.WOOL.getId()) {
                heightAxis = Axis.X;
                origin = new Location(world, x1, y1+1, z1);
            } else if (world.getBlockTypeIdAt(x1, y1-1, z1)==Material.WOOL.getId()) {
                heightAxis = Axis.X;
                origin = new Location(world, x1, y1-1, z1);
            } else if (world.getBlockTypeIdAt(x1+1, y1, z1)==Material.WOOL.getId()) {
                heightAxis = Axis.Y;
                origin = new Location(world, x1+1, y1, z1);
            } else if (world.getBlockTypeIdAt(x1-1, y1, z1)==Material.WOOL.getId()) {
                heightAxis = Axis.Y;
                origin = new Location(world, x1-1, y1, z1-1);
            } else throw new IllegalArgumentException("Can't find origin wool block.");            
        } else throw new IllegalArgumentException("Both interface blocks must be on the same plane.");        
        
        ScreenDescription ds = new ScreenDescription();
        ds.origin = origin;
        ds.physicalHeight = phyHeight;
        ds.physicalWidth = phyWidth;
        ds.heightAxis = heightAxis;
        ds.widthAxis = widthAxis;
        return ds;
    }
    
    private static Location[] findPixelBlocks(Location origin, int x, int y, Axis widthAxis, Axis heightAxis, int pixelWidth, int pixelHeight) {
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
                        ret[i] = new Location(origin.getWorld(), x1+dx+(xsign>0?ix:ix*-1), y1+dy+(ysign>0?iy:iy*-1), z1);
                        i++;
                    }
                }
            } else { // Axis.Z
                for (int ix=0; ix<pixelWidth; ix++) {
                    for (int iy=0; iy<pixelHeight; iy++) {
                        ret[i] = new Location(origin.getWorld(), x1+dx+(xsign>0?ix:ix*-1), y1, z1+dy+(ysign>0?iy:iy*-1));
                        i++;
                    }
                }
            }
        } else if (widthAxis==Axis.Y) {
            if (heightAxis==Axis.X) {
                for (int ix=0; ix<pixelWidth; ix++) {
                    for (int iy=0; iy<pixelHeight; iy++) {
                        ret[i] = new Location(origin.getWorld(), x1+dy+(ysign>0?iy:iy*-1), y1+dx+(xsign>0?ix:ix*-1), z1);
                        i++;
                    }
                }
            } else {// Axis.Z
                for (int ix=0; ix<pixelWidth; ix++) {
                    for (int iy=0; iy<pixelHeight; iy++) {
                        ret[i] = new Location(origin.getWorld(), x1, y1+dx+(xsign>0?ix:ix*-1), z1+dy+(ysign>0?iy:iy*-1));
                        i++;
                    }
                }
            }
        } else { // Axis.Z
            if (heightAxis==Axis.Y) {
                for (int ix=0; ix<pixelWidth; ix++) {
                    for (int iy=0; iy<pixelHeight; iy++) {
                        ret[i] = new Location(origin.getWorld(), x1, y1+dy+(ysign>0?iy:iy*-1), z1+dx+(xsign>0?ix:ix*-1));
                        i++;
                    }
                }
            } else { // Axis.X
                for (int ix=0; ix<pixelWidth; ix++) {
                    for (int iy=0; iy<pixelHeight; iy++) {
                        ret[i] = new Location(origin.getWorld(), x1+dy+(ysign>0?iy:iy*-1), y1, z1+dx+(xsign>0?ix:ix*-1));
                        i++;
                    }
                }
            }
        }

        return ret;
    }
    
    public static int calculateBitLength(int numOfValues) {
        return (int)Math.ceil(Math.log(numOfValues)/Math.log(2));
    }        
}
