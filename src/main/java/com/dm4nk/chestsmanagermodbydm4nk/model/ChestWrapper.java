package com.dm4nk.chestsmanagermodbydm4nk.model;

import com.google.common.collect.Streams;
import com.google.common.primitives.Ints;
import lombok.Getter;
import lombok.NonNull;
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
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Collection;
import java.util.stream.IntStream;

import static com.dm4nk.chestsmanagermodbydm4nk.util.Constants.CHEST_CAPACITY;
import static com.dm4nk.chestsmanagermodbydm4nk.util.Constants.DOUBLE_CHEST_CAPACITY;

@Slf4j
@Getter
@Setter
public class ChestWrapper {

    private final BlockPos blockPos;
    private final ChestBlockEntity entity;
    private final BlockState blockState;
    private final IItemHandler handler;

    private final int leftBorder;
    private final int rightBorder;

    public ChestWrapper(@NonNull BlockPos blockPos, @NonNull ChestBlockEntity entity, @NonNull BlockState blockState) {
        this.blockPos = blockPos;
        this.entity = entity;
        this.blockState = blockState;

        this.handler = entity.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.NORTH).resolve()
                .orElseThrow(() -> new IllegalStateException(String.format("No capabilities available for entity %s", entity)));

        this.leftBorder = blockState.getValue(BlockStateProperties.CHEST_TYPE).equals(ChestType.RIGHT) ? CHEST_CAPACITY : 0;

        this.rightBorder = blockState.getValue(BlockStateProperties.CHEST_TYPE).equals(ChestType.RIGHT) ? DOUBLE_CHEST_CAPACITY : CHEST_CAPACITY;
    }

    public Collection<ItemStack> extractInventory() {
        Collection<ItemStack> inventory = IntStream
                .range(leftBorder, rightBorder)
                .mapToObj(position -> handler.extractItem(position, Integer.MAX_VALUE, false).copy())
                .filter(itemStack -> !itemStack.isEmpty())
                .toList();

        log.debug("Inventory Chest{} = {}", getCoordinatesAsString(), inventory);
        return inventory;
    }

    public void setInventory(Collection<ItemStack> inventory) {
        log.debug("inventory: {}", inventory);

        if (inventory.size() > CHEST_CAPACITY) {
            throw new IllegalArgumentException("Too much inventory items");
        }

        Streams.zip(
                        IntStream.range(leftBorder, rightBorder).boxed(),
                        inventory.stream(),
                        ImmutablePair::of
                )
                .forEach(pair -> handler.insertItem(Ints.checkedCast(pair.getLeft()), pair.getValue(), false));

        log.debug("setInventory done");
    }

    public String getCoordinatesAsString() {
        return String.format("(x:%s y:%s z:%s)", blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    @Override
    public String toString() {
        return getCoordinatesAsString();
    }
}
