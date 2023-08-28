package com.dm4nk.chestsmanagermodbydm4nk.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

public class ManageChestsCommand {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Integer RADIUS = 10;

    public ManageChestsCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("manage").then(Commands.literal("chests").executes((command) -> {
            LOGGER.info("ManageChestsCommand constructor runs! Command: {}", command);
            return this.manageChests(command.getSource());
        })));
    }

    private int manageChests(CommandSourceStack command) throws CommandSyntaxException {
        LOGGER.info("manageChests runs! Command: {}", command);
        Optional<ServerPlayer> player = Optional.ofNullable(command.getPlayer());

        BlockPos playerPos = player
                .orElseThrow(() -> new RuntimeException("Player is null for command " + command))
                .getOnPos();

        ServerLevel serverlevel = command.getLevel();

        Collection<ImmutablePair<ChestBlock, BlockPos>> chests = getNearbyChests(playerPos, RADIUS, serverlevel);
        String chestsCoordinates = chests.stream()
                .map(Pair::getValue)
                .map(chestPos -> String.format("(x:%s y:%s z:%s)", chestPos.getX(), chestPos.getY(), chestPos.getZ()))
                .collect(Collectors.joining(", "));

        LOGGER.info("Got all chests: {}", chests);
        command.sendSuccess(() -> Component.translatable("commands.chestsmanagermodbydm4nk.gotallchests", chestsCoordinates), true);
        return 1;
    }

    private Collection<ImmutablePair<ChestBlock, BlockPos>> getNearbyChests(BlockPos pos, Integer radius, ServerLevel serverlevel) {
        LOGGER.info("getNearbyChests runs! pos: {}, radius: {}", pos, radius);

        List<ImmutablePair<ChestBlock, BlockPos>> blocks = new LinkedList<>();

        for (int x = pos.getX() - radius; x <= pos.getX() + radius; x++) {
            for (int y = pos.getY() - radius; y <= pos.getY() + radius; y++) {
                for (int z = pos.getZ() - radius; z <= pos.getZ() + radius; z++) {
                    BlockPos currentPosition = new BlockPos(x, y, z);
                    BlockState blockState = serverlevel.getBlockState(currentPosition);
                    Block block = blockState.getBlock();

                    if (block instanceof ChestBlock) {
                        LOGGER.info("Got chest! pos: {}", pos);
                        blocks.add(ImmutablePair.of((ChestBlock) block, currentPosition));
                    }
                }
            }
        }

        return blocks;
    }
}
