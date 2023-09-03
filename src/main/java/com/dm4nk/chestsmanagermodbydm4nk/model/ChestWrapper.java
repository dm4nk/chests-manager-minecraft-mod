package com.dm4nk.chestsmanagermodbydm4nk.model;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.IntStream;

@Slf4j
@Getter
@Setter
public class ChestWrapper {

    private final BlockPos blockPos;
    private final ChestBlockEntity entity;
    private final BlockState blockState;
    private final IItemHandler handler;

    public ChestWrapper(BlockPos blockPos, ChestBlockEntity entity, BlockState blockState) {
        this.blockPos = blockPos;
        this.entity = entity;
        this.blockState = blockState;
        this.handler = entity.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.NORTH).resolve()
                .orElseThrow(() -> new IllegalStateException(String.format("No capabilities available for entity %s", entity)));
    }

    public String getCoordinatesAsString() {
        return String.format("(x:%s y:%s z:%s)", blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    public Collection<ItemStack> getInventory() {
        log.debug("entity: {}", entity);

        if (entity == null) {
            return Collections.emptyList();
        }

        log.debug("Capacity: {}", handler.getSlots());


        int leftBorder = blockState.getValue(BlockStateProperties.CHEST_TYPE).equals(ChestType.RIGHT) ? 27 : 0;
        int rightBorder = blockState.getValue(BlockStateProperties.CHEST_TYPE).equals(ChestType.RIGHT) ? 53 : 26;


        Collection<ItemStack> inventory = IntStream
                .range(leftBorder, rightBorder)
                .mapToObj(position -> handler.getStackInSlot(position).copy())
                .filter(itemStack -> !itemStack.isEmpty())
                .toList();

        log.debug("Inventory Chest{} = {}", getCoordinatesAsString(), inventory);
        return inventory;
    }

    @Override
    public String toString() {
        return getCoordinatesAsString();
    }
}
