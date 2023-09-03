package com.dm4nk.chestsmanagermodbydm4nk.commands;

import com.dm4nk.chestsmanagermodbydm4nk.model.ChestWrapper;
import com.mojang.brigadier.CommandDispatcher;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Slf4j
public class ManageChestsCommand {
    private static final Integer RADIUS = 10;

    public ManageChestsCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("manage").then(Commands.literal("chests").executes((command) -> {
            log.info("ManageChestsCommand constructor runs! Command: {}", command);
            return this.manageChests(command.getSource());
        })));
    }

    private int manageChests(CommandSourceStack command) {
        log.info("manageChests runs! Command: {}", command);
        Optional<ServerPlayer> player = Optional.ofNullable(command.getPlayer());

        BlockPos playerPos = player
                .orElseThrow(() -> new RuntimeException("Player is null for command " + command))
                .getOnPos();

        ServerLevel serverlevel = command.getLevel();

        Collection<ChestWrapper> chests = getNearbyChests(playerPos, serverlevel);

        log.info("Got all chests: {}", chests);
        command.sendSuccess(() -> Component.translatable("commands.chestsmanagermodbydm4nk.gotallchests", chests), true);

        chests.forEach(chest -> {
            Collection<ItemStack> inventory = chest.getInventory();
            command.sendSuccess(() -> Component.translatable("commands.chestsmanagermodbydm4nk.chestinventory", chest.getCoordinatesAsString(), inventory), true);
        });

        return 1;
    }

    private Collection<ChestWrapper> getNearbyChests(BlockPos pos, ServerLevel serverlevel) {
        log.info("getNearbyChests runs! pos: {}, radius: {}", pos, ManageChestsCommand.RADIUS);
        Set<ChestWrapper> blocks = new HashSet<>();

        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int y = -RADIUS; y <= RADIUS; y++) {
                for (int z = -RADIUS; z <= RADIUS; z++) {
                    BlockPos currentPosition = pos.offset(x, y, z);
                    BlockState blockState = serverlevel.getBlockState(currentPosition);
                    ChestBlockEntity entity = (ChestBlockEntity) serverlevel.getBlockEntity(currentPosition);
                    Block block = blockState.getBlock();

                    if (block instanceof ChestBlock) {
//                        blockState.getValue(BlockStateProperties.CHEST_TYPE);
                        log.info("Got chest! pos: {}", pos);
                        blocks.add(new ChestWrapper(currentPosition, entity, blockState));
                    }
                }
            }
        }


        return blocks;
    }
}
