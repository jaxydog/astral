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

package dev.jaxydog.content.block.custom;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.jaxydog.Astral;
import dev.jaxydog.content.item.CustomItems;
import dev.jaxydog.datagen.ModelGenerator;
import dev.jaxydog.datagen.TagGenerator;
import dev.jaxydog.datagen.TextureGenerator;
import dev.jaxydog.register.Registered.Client;
import dev.jaxydog.utility.AstralModel;
import dev.jaxydog.utility.ColorUtil.Rgb;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.data.client.Model;
import net.minecraft.data.client.ModelIds;
import net.minecraft.data.client.Models;
import net.minecraft.data.client.TextureMap;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

import java.awt.image.BufferedImage;
import java.util.Optional;

/**
 * Implements dyed amethyst blocks.
 *
 * @author Jaxydog
 */
public class DyeableAmethystClusterBlock extends DyeableAmethystBlock implements Client {

    /** Controls whether the block is waterlogged. */
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    /** Controls the direction the block is facing. */
    public static final DirectionProperty FACING = Properties.FACING;
    /** A block tag containing all amethyst clusters. */
    public static final TagKey<Block> AMETHYST_CLUSTERS = TagKey.of(Registries.BLOCK.getKey(),
        Astral.getId("amethyst_clusters")
    );
    /** An item tag containing all amethyst clusters. */
    public static final TagKey<Item> AMETHYST_CLUSTER_ITEMS = TagKey.of(Registries.ITEM.getKey(),
        Astral.getId("amethyst_clusters")
    );

    private final VoxelShape upShape;
    private final VoxelShape downShape;
    private final VoxelShape northShape;
    private final VoxelShape southShape;
    private final VoxelShape eastShape;
    private final VoxelShape westShape;
    private final Variant variant;

    @SuppressWarnings("SuspiciousNameCombination")

    public DyeableAmethystClusterBlock(String rawId, Settings settings, DyeColor color, Variant variant) {
        super(rawId, settings, color);

        final int height = variant.getHeight();
        final int size = variant.getSize();

        this.upShape = Block.createCuboidShape(size, 0D, size, (16 - size), height, (16 - size));
        this.downShape = Block.createCuboidShape(size, (16 - height), size, (16 - size), 16, (16 - size));
        this.northShape = Block.createCuboidShape(size, size, (16 - height), (16 - size), (16 - size), 16);
        this.southShape = Block.createCuboidShape(size, size, 0D, (16 - size), (16 - size), height);
        this.eastShape = Block.createCuboidShape(0D, size, size, height, (16 - size), (16 - size));
        this.westShape = Block.createCuboidShape((16 - height), size, size, 16, (16 - size), (16 - size));
        this.variant = variant;
    }

    @SuppressWarnings("RedundantCast")
    private static Model getModel() {
        final Model model = ((AstralModel) Models.GENERATED).astral$copy();

        ((AstralModel) model).astral$addCustomJson("display", getModelDisplay());

        return model;
    }

    private static JsonObject getModelDisplay() {
        final JsonObject object = new JsonObject();
        final JsonObject head = new JsonObject();
        final JsonArray translation = new JsonArray(3);

        translation.add(0);
        translation.add(14);
        translation.add(-5);
        head.add("translation", translation);
        object.add("head", head);

        return object;
    }

    public Variant getVariant() {
        return this.variant;
    }

    /**
     * Returns this cluster's associated item.
     *
     * @return The associated item.
     */
    public Item getItem() {
        return switch (this.variant) {
            case CLUSTER -> CustomItems.DYEABLE_AMETHYST_CLUSTERS.get(this.getColor());
            case LARGE_BUD -> CustomItems.DYEABLE_LARGE_AMETHYST_BUDS.get(this.getColor());
            case MEDIUM_BUD -> CustomItems.DYEABLE_MEDIUM_AMETHYST_BUDS.get(this.getColor());
            case SMALL_BUD -> CustomItems.DYEABLE_SMALL_AMETHYST_BUDS.get(this.getColor());
        };
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockState getStateForNeighborUpdate(
        BlockState state,
        Direction direction,
        BlockState neighborState,
        WorldAccess world,
        BlockPos pos,
        BlockPos neighborPos
    ) {
        if (state.get(WATERLOGGED)) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }

        if (direction == state.get(FACING).getOpposite() && !state.canPlaceAt(world, pos)) {
            return Blocks.AIR.getDefaultState();
        } else {
            return state;
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public FluidState getFluidState(BlockState state) {
        if (state.get(WATERLOGGED)) {
            return Fluids.WATER.getStill(false);
        } else {
            return Fluids.EMPTY.getDefaultState();
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        final Direction direction = state.get(FACING);
        final BlockPos blockPos = pos.offset(direction.getOpposite());

        return world.getBlockState(blockPos).isSideSolidFullSquare(world, blockPos, direction);
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(FACING)) {
            case UP -> this.upShape;
            case DOWN -> this.downShape;
            case NORTH -> this.northShape;
            case SOUTH -> this.southShape;
            case EAST -> this.eastShape;
            case WEST -> this.westShape;
        };
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        final World world = ctx.getWorld();
        final BlockPos blockPos = ctx.getBlockPos();
        final boolean waterlogged = world.getFluidState(blockPos).getFluid() == Fluids.WATER;

        return this.getDefaultState().with(WATERLOGGED, waterlogged).with(FACING, ctx.getSide());
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, FACING);
    }

    @Override
    public void registerClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(this, RenderLayer.getCutout());
    }

    @Override
    public void generate() {
        final Model itemModel = getModel();

        ModelGenerator.getInstance().generateBlock(g -> g.registerAmethyst(this));
        ModelGenerator.getInstance().generateItem(g -> {
            final Identifier identifier = ModelIds.getItemModelId(this.getItem());

            itemModel.upload(identifier, TextureMap.layer0(this), g.writer);
        });

        TagGenerator.getInstance().generate(AMETHYST_CLUSTERS, b -> b.add(this));
        TagGenerator.getInstance().generate(AMETHYST_CLUSTER_ITEMS, b -> b.add(this.getItem()));
        TextureGenerator.getInstance().generate(Registries.BLOCK.getKey(), instance -> {
            final Optional<BufferedImage> maybeImage = instance.getImage(this.getVariant().getBaseId());

            if (maybeImage.isEmpty()) return;

            final BufferedImage image = maybeImage.get();
            final BufferedImage generated = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
            final DyeColor color = this.getColor();

            for (int y = 0; y < image.getHeight(); y += 1) {
                for (int x = 0; x < image.getWidth(); x += 1) {
                    final int argb = image.getRGB(x, y);

                    if ((argb & 0xFF000000) == 0) continue;

                    generated.setRGB(x, y, convertColor(new Rgb(argb), color));
                }
            }

            finalColorPass(generated, color);

            instance.generate(this.getRegistryId(), generated);
        });
    }

    public enum Variant {

        CLUSTER("amethyst_cluster", 7, 3),

        LARGE_BUD("large_amethyst_bud", 5, 3),

        MEDIUM_BUD("medium_amethyst_bud", 4, 3),

        SMALL_BUD("small_amethyst_bud", 3, 4);

        private final String baseId;
        private final int height;
        private final int size;

        Variant(String baseId, int height, int size) {
            this.baseId = baseId;
            this.height = height;
            this.size = size;
        }

        public String getBaseId() {
            return this.baseId;
        }

        public int getHeight() {
            return this.height;
        }

        public int getSize() {
            return this.size;
        }

    }

}
