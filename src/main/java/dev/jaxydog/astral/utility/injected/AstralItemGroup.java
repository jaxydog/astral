/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright © 2024 Jaxydog
 *
 * This file is part of Astral.
 *
 * Astral is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Astral is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with Astral. If not, see <https://www.gnu.org/licenses/>.
 */

package dev.jaxydog.astral.utility.injected;

import net.minecraft.item.ItemStack;

/**
 * Extends item groups.
 *
 * @author Jaxydog
 * @since 1.7.0
 */
public interface AstralItemGroup {

    /**
     * Returns the item group's icon, computed using the provided delta.
     *
     * @param delta The time passed since the last update.
     *
     * @return An item stack.
     *
     * @since 1.7.0
     */
    default ItemStack astral$getIcon(float delta) {
        return ItemStack.EMPTY;
    }

}
