package org.redstonechips.basiccircuits.screen;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

/**
 *
 * @author Tal Eisenberg
 */
public class Screen {

    public static final Material[] materials = new Material[] {
        Material.WHITE_WOOL,
        Material.ORANGE_WOOL,
        Material.MAGENTA_WOOL,
        Material.LIGHT_BLUE_WOOL,
        Material.YELLOW_WOOL,
        Material.LIME_WOOL,
        Material.PINK_WOOL,
        Material.GRAY_WOOL,
        Material.LIGHT_GRAY_WOOL,
        Material.CYAN_WOOL,
        Material.PURPLE_WOOL,
        Material.BLUE_WOOL,
        Material.BROWN_WOOL,
        Material.GREEN_WOOL,
        Material.RED_WOOL,
        Material.BLACK_WOOL,
    };

    public enum Axis {X,Y,Z}

    public Material ColorSel(int colornum) {
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

    private final ScreenDescription ds;

    private byte[] colorIndex = null;

    private int colorLength = 4;

    private final Location[][][] pixels;
    private final byte[][] memory;

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

    public int getXLength() {
        return calculateBitLength(ds.addrWidth);
    }

    public int getYLength() {
        return calculateBitLength(ds.addrHeight);
    }

    public Location[][][] getPixelBlocks() {
        return pixels;
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
                BlockState bState = b.getState();
                for (Material m : Screen.materials) {
                    if (b.getType()==m) {
                        bState.setType(ColorSel(color));
                        bState.update(true, true);
                        break;
                    }
                }
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

        Axis widthAxis, heightAxis = null;
        Location origin = null;

        if (dx==0 && dy!=0 && dz!=0) { // zy plane
            phyWidth = (dz+1)*zsign;
            phyHeight = (dy+1)*ysign;
            widthAxis = Axis.Z;
            heightAxis = Axis.Y;

            for (Material m : Screen.materials) {
                if (world.getBlockAt(x1+1,y1,z1).getType()==m) {
                    origin = new Location(world, x1+1, y1, z1);
                    break;
                } else if (world.getBlockAt(x1-1,y1,z1).getType()==m) {
                    origin = new Location(world, x1-1, y1, z1);
                    break;
                }
            }
            if (origin == null)
                throw new IllegalArgumentException("Can't find origin screen block.");

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

            for (Material m : Screen.materials) {
                if (world.getBlockAt(x1,y1+1,z1).getType()==m) {
                    origin = new Location(world, x1, y1+1, z1);
                    break;
                } else if (world.getBlockAt(x1,y1-1,z1).getType()==m) {
                    origin = new Location(world, x1, y1-1, z1);
                    break;
                }
            }
            if (origin == null)
                throw new IllegalArgumentException("Can't find origin screen block.");

        } else if (dx!=0 && dy!=0 && dz==0) { // xy plane
            phyWidth = (dx+1)*xsign;
            phyHeight = (dy+1)*ysign;
            widthAxis = Axis.X;
            heightAxis = Axis.Y;

            for (Material m : Screen.materials) {
                if (world.getBlockAt(x1,y1,z1+1).getType()==m) {
                    origin = new Location(world, x1, y1, z1+1);
                    break;
                } else if (world.getBlockAt(x1,y1,z1-1).getType()==m) {
                    origin = new Location(world, x1, y1, z1-1);
                    break;
                }
            }
            if (origin == null)
                throw new IllegalArgumentException("Can't find origin screen block.");

        } else if (dx==0 && dy!=0 && dz==0) { // y line
            phyWidth = (dy+1)*ysign;
            phyHeight = 1;
            widthAxis = Axis.Y;

            for (Material m : Screen.materials) {
                if (world.getBlockAt(x1+1,y1,z1).getType()==m) {
                    heightAxis = Axis.Z;
                    origin = new Location(world, x1+1, y1, z1);
                    break;
                } else if (world.getBlockAt(x1-1,y1,z1).getType()==m) {
                    heightAxis = Axis.Z;
                    origin = new Location(world, x1-1, y1, z1);
                    break;
                } else if (world.getBlockAt(x1,y1,z1+1).getType()==m) {
                    heightAxis = Axis.X;
                    origin = new Location(world, x1, y1, z1+1);
                    break;
                } else if (world.getBlockAt(x1,y1,z1-1).getType()==m) {
                    heightAxis = Axis.X;
                    origin = new Location(world, x1, y1, z1-1);
                    break;
                }
            }
            if (origin == null)
                throw new IllegalArgumentException("Can't find origin screen block.");

        } else if (dx!=0 && dy==0 && dz==0) { // x line
            phyWidth = (dx+1)*xsign;
            phyHeight = 1;
            widthAxis = Axis.X;

            for (Material m : Screen.materials) {
                if (world.getBlockAt(x1,y1+1,z1).getType()==m) {
                    heightAxis = Axis.Z;
                    origin = new Location(world, x1, y1+1, z1);
                    break;
                } else if (world.getBlockAt(x1,y1-1,z1).getType()==m) {
                    heightAxis = Axis.Z;
                    origin = new Location(world, x1, y1-1, z1);
                    break;
                } else if (world.getBlockAt(x1,y1,z1+1).getType()==m) {
                    heightAxis = Axis.Y;
                    origin = new Location(world, x1, y1, z1+1);
                    break;
                } else if (world.getBlockAt(x1,y1,z1-1).getType()==m) {
                    heightAxis = Axis.Y;
                    origin = new Location(world, x1, y1, z1-1);
                    break;
                }
            }
            if (origin == null)
                throw new IllegalArgumentException("Can't find origin screen block.");

        } else if (dx==0 && dy==0 && dz!=0) { // z line
            phyWidth = (dz+1)*zsign;
            phyHeight = 1;
            widthAxis = Axis.Z;

         for (Material m : Screen.materials) {
                if (world.getBlockAt(x1,y1+1,z1).getType()==m) {
                    heightAxis = Axis.X;
                    origin = new Location(world, x1, y1+1, z1);
                    break;
                } else if (world.getBlockAt(x1,y1-1,z1).getType()==m) {
                    heightAxis = Axis.X;
                    origin = new Location(world, x1, y1-1, z1);
                    break;
                } else if (world.getBlockAt(x1+1,y1,z1).getType()==m) {
                    heightAxis = Axis.Y;
                    origin = new Location(world, x1+1, y1, z1);
                    break;
                } else if (world.getBlockAt(x1-1,y1,z1).getType()==m) {
                    heightAxis = Axis.Y;
                    origin = new Location(world, x1-1, y1, z1);
                    break;
                }
            }
            if (origin == null)
                throw new IllegalArgumentException("Can't find origin screen block.");
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