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

import com.google.gson.JsonElement;
import net.minecraft.data.client.Model;

import java.util.Map;

/**
 * Extends {@link net.minecraft.data.client.Model}.
 *
 * @author Jaxydog
 * @since 1.7.0
 */
public interface AstralModel {

    /**
     * Adds a custom JSON entry to this model.
     *
     * @param key The JSON key.
     * @param element The JSON value.
     *
     * @since 1.7.0
     */
    void astral$addCustomJson(String key, JsonElement element);

    /**
     * Adds a custom JSON entry to this model.
     *
     * @param map The JSON elements.
     *
     * @since 1.7.0
     */
    void astral$addCustomJson(Map<String, JsonElement> map);

    /**
     * Copies the model.
     *
     * @return A copy of this model.
     *
     * @since 1.7.0
     */
    Model astral$copy();

}
