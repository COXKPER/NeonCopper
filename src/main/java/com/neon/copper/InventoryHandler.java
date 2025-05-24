package com.neon.copper;

import com.neon.copper.Userdata;
import net.goldenstack.window.*;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.PlayerInventory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minestom.server.item.ItemStack;
import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.BinaryTagIO.Compression;

public class InventoryHandler {

    // Save inventory to same file after position
    public static void save(Player player, DataOutputStream out) throws IOException {
        PlayerInventory inv = player.getInventory();

        // Save hotbar (slots 0-8)
        for (int i = 0; i < 9; i++) {
            writeItem(out, inv.getItemStack(i));
        }

        // Save main inventory (slots 9–35)
        for (int i = 9; i <= 35; i++) {
            writeItem(out, inv.getItemStack(i));
        }

        // Save armor
        writeItem(out, inv.getItemStack(36)); // boots
        writeItem(out, inv.getItemStack(37)); // leggings
        writeItem(out, inv.getItemStack(38)); // chestplate
        writeItem(out, inv.getItemStack(39)); // helmet

        // Save offhand (slot 40)
        writeItem(out, inv.getItemStack(40));
    }

    public static void load(Player player, DataInputStream in) throws IOException {
        PlayerInventory inv = player.getInventory();

        // Load hotbar
        for (int i = 0; i < 9; i++) {
            inv.setItemStack(i, readItem(in));
        }

        // Load main inventory
        for (int i = 9; i <= 35; i++) {
            inv.setItemStack(i, readItem(in));
        }

        // Load armor
        inv.setItemStack(36, readItem(in)); // boots
        inv.setItemStack(37, readItem(in)); // leggings
        inv.setItemStack(38, readItem(in)); // chestplate
        inv.setItemStack(39, readItem(in)); // helmet

        // Load offhand
        inv.setItemStack(40, readItem(in));
    }

    // --- Utility methods for reading/writing item stacks ---

    public static void writeItem(DataOutputStream out, ItemStack item) throws IOException {
        if (item == null || item.isAir()) {
            out.writeBoolean(false);
            return;
        }

        out.writeBoolean(true);
        CompoundBinaryTag nbt = item.toItemNBT();

        // Serialize NBT to bytes
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        BinaryTagIO.writer().write(nbt, byteOut, Compression.NONE);
        byte[] raw = byteOut.toByteArray();

        out.writeInt(raw.length);
        out.write(raw);
    }

    public static ItemStack readItem(DataInputStream in) throws IOException {
        boolean hasItem = in.readBoolean();
        if (!hasItem) return ItemStack.AIR;

        int length = in.readInt();
        byte[] raw = new byte[length];
        in.readFully(raw);

        ByteArrayInputStream byteIn = new ByteArrayInputStream(raw);
        CompoundBinaryTag tag = (CompoundBinaryTag) BinaryTagIO.reader().read(byteIn, Compression.NONE);

        return ItemStack.fromItemNBT(tag);
    }
}
