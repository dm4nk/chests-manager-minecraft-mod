package com.dm4nk.chestsmanagermodbydm4nk.events;

import com.dm4nk.chestsmanagermodbydm4nk.ExampleMod;
import com.dm4nk.chestsmanagermodbydm4nk.commands.ManageChestsCommand;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.command.ConfigCommand;

@Mod.EventBusSubscriber(modid = ExampleMod.MOD_ID)
public class ModEvents {

    @SubscribeEvent
    public static void onCommandsRegister(RegisterCommandsEvent event) {
        new ManageChestsCommand(event.getDispatcher());

        ConfigCommand.register(event.getDispatcher());
    }
}
