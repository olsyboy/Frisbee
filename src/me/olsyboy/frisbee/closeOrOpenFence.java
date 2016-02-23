package me.olsyboy.frisbee;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.material.Gate;

import java.util.ArrayList;


public class closeOrOpenFence {
    Main main;public closeOrOpenFence(Main main) {
        this.main = main;
    }

    public void getBlocksOpen(Location loc,int radius){
        ArrayList<Location> bl = new ArrayList<Location>();
        World world = loc.getWorld();
        for (int x = -radius; x < radius; x++) {
            for (int y = -radius; y < radius; y++) {
                for (int z = -radius; z < radius; z++) {
                    Block block = world.getBlockAt(loc.getBlockX()+x, loc.getBlockY()+y, loc.getBlockZ()+z);
                    if(block.getType() == Material.BIRCH_FENCE_GATE) {
                        BlockState bS = block.getState();bS.setRawData((byte) 0x6);bS.update();
                        bl.add(block.getLocation());
                    }
                }
            }
        }
    }
    public void getBlocksClose(Location loc,int radius){
        ArrayList<Location> bl = new ArrayList<Location>();
        World world = loc.getWorld();
        for (int x = -radius; x < radius; x++) {
            for (int y = -radius; y < radius; y++) {
                for (int z = -radius; z < radius; z++) {
                    Block block = world.getBlockAt(loc.getBlockX()+x, loc.getBlockY()+y, loc.getBlockZ()+z);
                    if(block.getType() == Material.BIRCH_FENCE_GATE) {
                        BlockState bS = block.getState();bS.setRawData((byte) 0);bS.update();
                        bl.add(block.getLocation());
                    }
                }
            }
        }
    }
}
