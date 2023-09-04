package com.dm4nk.chestsmanagermodbydm4nk.commands;

import com.dm4nk.chestsmanagermodbydm4nk.model.ChestWrapper;
import com.dm4nk.chestsmanagermodbydm4nk.util.ItemStackComparator;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import com.mojang.brigadier.CommandDispatcher;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.*;
import java.util.stream.Stream;

import static com.dm4nk.chestsmanagermodbydm4nk.util.Constants.RADIUS;

@Slf4j
public class ManageChestsCommand {
    public ManageChestsCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("manage").then(Commands.literal("chests").executes((command) -> {
            log.info("ManageChestsCommand constructor runs! Command: {}", command);
            return this.manageChests(command.getSource());
        })));
    }

    private int manageChests(CommandSourceStack command) {
        log.info("manageChests runs! Command: {}", command);
        BlockPos playerPos = Optional.ofNullable(command.getPlayer())
                .orElseThrow(() -> new RuntimeException("Player is null for command " + command))
                .getOnPos();
        ServerLevel serverlevel = command.getLevel();

        ImmutableCollection<ChestWrapper> chests = getNearbyChests(playerPos, serverlevel);
        log.info("Got all chests: {}", chests);

        List<ItemStack> allItemStacksSorted = chests.stream()
                .flatMap(chestWrapper -> chestWrapper.extractInventory().stream())
                .sorted(new ItemStackComparator())
                .toList();
        log.info("Got all items: {}", allItemStacksSorted);

        List<List<ItemStack>> inventories = Lists.partition(allItemStacksSorted, 27);
        log.info("Calculated inventories: {}", inventories);

        List<List<ItemStack>> emptyChests = Collections.nCopies(chests.size() - inventories.size(), Collections.emptyList());
        log.info("Calculated Empty Chests: {}", emptyChests);

        List<List<ItemStack>> inventoriesWithEmpty = Stream.of(inventories, emptyChests)
                .flatMap(Collection::stream)
                .toList();
        log.info("Added empty chests: {}", inventoriesWithEmpty);

        Streams.zip(
                        chests.stream(),
                        inventoriesWithEmpty.stream(),
                        ImmutablePair::of
                )
                .forEach(pair -> pair.getLeft().setInventory(pair.getRight()));

        command.sendSuccess(() -> Component.translatable("commands.chestsmanagermodbydm4nk.finished"), true);
        return 1;
    }

    private ImmutableCollection<ChestWrapper> getNearbyChests(BlockPos pos, ServerLevel serverlevel) {
        log.info("getNearbyChests runs! pos: {}, radius: {}", pos, RADIUS);
        List<ChestWrapper> blocks = new LinkedList<>();

        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int y = -RADIUS; y <= RADIUS; y++) {
                for (int z = -RADIUS; z <= RADIUS; z++) {
                    BlockPos currentPosition = pos.offset(x, y, z);
                    BlockState blockState = serverlevel.getBlockState(currentPosition);
                    ChestBlockEntity entity = (ChestBlockEntity) serverlevel.getBlockEntity(currentPosition);
                    Block block = blockState.getBlock();

                    if (block instanceof ChestBlock) {
                        log.info("Got chest! pos: {}", pos);
                        blocks.add(new ChestWrapper(currentPosition, entity, blockState));
                    }
                }
            }
        }


        return ImmutableList.copyOf(blocks);
    }
}
