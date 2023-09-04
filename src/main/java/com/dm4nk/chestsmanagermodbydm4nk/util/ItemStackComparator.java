package com.dm4nk.chestsmanagermodbydm4nk.util;

import com.google.common.primitives.Ints;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;

@Slf4j
public class ItemStackComparator implements Comparator<ItemStack> {
    @Override
    public int compare(final ItemStack o1, final ItemStack o2) {
        log.debug("Comparing {} and {}", o1, o2);
        if (o1 == null && o2 == null) {
            return 0;
        } else if (o1 == null) {
            return 1;
        } else if (o2 == null) {
            return -1;
        }

        int creativeModeTabDiff =  getCreativeTabId(o2) - getCreativeTabId(o1);
        if (creativeModeTabDiff != 0) {
            log.debug("Compared by creative tab diff: {}", creativeModeTabDiff);
            return creativeModeTabDiff;
        }

        int rarityDiff = o1.getRarity().compareTo(o2.getRarity());
        if (rarityDiff != 0) {
            log.debug("Compared by rarity diff: {}", rarityDiff);
            return rarityDiff;
        }

        int idDiff = Item.getId(o1.getItem()) - Item.getId(o2.getItem());
        log.debug("Compared by id diff: {}", idDiff);
        return idDiff;
    }

    private static int getCreativeTabId(final ItemStack itemStack) {
        int result = Ints.checkedCast(
                CreativeModeTabs.allTabs().stream()
                        .takeWhile(tab -> tab.contains(itemStack))
                        .count()
        );
        log.debug("getCreativeTabId({}) {}", itemStack, result);
        return result;
    }
}
