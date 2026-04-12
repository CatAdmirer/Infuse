package com.catadmirer.infuseSMP.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.catadmirer.infuseSMP.managers.ParticleManager;

public class Draw implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only a player can use this command!", NamedTextColor.RED));
            return true;
        }

        if (args.length < 6) {
            sender.sendMessage(Component.text("Missing some coordinates", NamedTextColor.RED));
            return true;
        }

        Location start = player.getLocation();
        Location end = player.getLocation();

        try {
            start.setX(Double.valueOf(args[0]));
        } catch (NumberFormatException err) {
            if (!args[0].contains("~")) {
                System.out.println("Invalid x1 coordinate!");
                err.printStackTrace();
                return true;
            }

            String offset = args[0].substring(1);
            if (!offset.isEmpty()) {
                try {
                    start.add(Double.valueOf(offset), 0, 0);
                } catch (NumberFormatException err2) {
                    System.out.println("Invalid x1 coordinate!");
                    err2.printStackTrace();
                }
            }
        }

        try {
            start.setY(Double.valueOf(args[1]));
        } catch (NumberFormatException err) {
            if (!args[1].contains("~")) {
                System.out.println("Invalid y1 coordinate!");
                err.printStackTrace();
                return true;
            }

            String offset = args[1].substring(1);
            if (!offset.isEmpty()) {
                try {
                    start.add(0, Double.valueOf(offset), 0);
                } catch (NumberFormatException err2) {
                    System.out.println("Invalid y1 coordinate!");
                    err2.printStackTrace();
                }
            }
        }

        try {
            start.setZ(Double.valueOf(args[2]));
        } catch (NumberFormatException err) {
            if (!args[2].contains("~")) {
                System.out.println("Invalid z1 coordinate!");
                err.printStackTrace();
                return true;
            }

            String offset = args[2].substring(1);
            if (!offset.isEmpty()) {
                try {
                    start.add(0, 0, Double.valueOf(offset));
                } catch (NumberFormatException err2) {
                    System.out.println("Invalid z1 coordinate!");
                    err2.printStackTrace();
                }
            }
        }

        try {
            end.setX(Double.valueOf(args[3]));
        } catch (NumberFormatException err) {
            if (!args[3].contains("~")) {
                System.out.println("Invalid x2 coordinate!");
                err.printStackTrace();
                return true;
            }

            String offset = args[3].substring(1);
            if (!offset.isEmpty()) {
                try {
                    end.add(Double.valueOf(offset), 0, 0);
                } catch (NumberFormatException err2) {
                    System.out.println("Invalid x2 coordinate!");
                    err2.printStackTrace();
                }
            }
        }

        try {
            end.setY(Double.valueOf(args[4]));
        } catch (NumberFormatException err) {
            if (!args[4].contains("~")) {
                System.out.println("Invalid y2 coordinate!");
                err.printStackTrace();
                return true;
            }

            String offset = args[4].substring(1);
            if (!offset.isEmpty()) {
                try {
                    end.add(0, Double.valueOf(offset), 0);
                } catch (NumberFormatException err2) {
                    System.out.println("Invalid y2 coordinate!");
                    err2.printStackTrace();
                }
            }
        }

        try {
            end.setZ(Double.valueOf(args[5]));
        } catch (NumberFormatException err) {
            if (!args[5].contains("~")) {
                System.out.println("Invalid z2 coordinate!");
                err.printStackTrace();
                return true;
            }

            String offset = args[5].substring(1);
            if (!offset.isEmpty()) {
                try {
                    end.add(0, 0, Double.valueOf(offset));
                } catch (NumberFormatException err2) {
                    System.out.println("Invalid z2 coordinate!");
                    err2.printStackTrace();
                }
            }
        }

        int count = 5;
        if (args.length >= 7) {
            try {
                count = Integer.valueOf(args[6]);
            } catch (NumberFormatException err) {
                System.out.println("Invalid particle count");
                err.printStackTrace();
            }
        }

        ParticleManager.drawLine(start, end, count);

        return true;
    }
}