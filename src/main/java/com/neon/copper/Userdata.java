package com.neon.copper;

import net.minestom.server.entity.Player;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;

import java.io.*;

public class Userdata {

public static void save(Player player) {
    File file = getUserFile(player);

    try (DataOutputStream out = new DataOutputStream(new FileOutputStream(file))) {
        Pos pos = player.getPosition();
        out.writeDouble(pos.x());
        out.writeDouble(pos.y());
        out.writeDouble(pos.z());
        out.writeFloat(pos.yaw());
        out.writeFloat(pos.pitch());
        out.writeUTF(player.getInstance().getDimensionName());

        InventoryHandler.save(player, out); 
    } catch (IOException e) {
        System.err.println("Failed to save: " + e.getMessage());
    }
}

public static void load(Player player, Instance instance) {
    File file = getUserFile(player);

    if (!file.exists()) return;

    try (DataInputStream in = new DataInputStream(new FileInputStream(file))) {
        double x = in.readDouble();
        double y = in.readDouble();
        double z = in.readDouble();
        float yaw = in.readFloat();
        float pitch = in.readFloat();
        String dimension = in.readUTF();

        player.setInstance(instance, new Pos(x, y, z, yaw, pitch));

        InventoryHandler.load(player, in); 
    } catch (IOException e) {
        System.err.println("Failed to load: " + e.getMessage());
    }
}


    private static File getUserFile(Player player) {
        File folder = new File("world/userdata/" + player.getUuid());
        if (!folder.exists()) folder.mkdirs();
        return new File(folder, "data.dat");
    }
}
