/*******************************************************************************
 * Copyright 2015-2016, the Biomes O' Plenty Team
 * 
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International Public License.
 * 
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/.
 ******************************************************************************/

package biomesoplenty.common.biome.overworld;

import java.util.Random;

import biomesoplenty.api.biome.BOPBiome;
import biomesoplenty.api.biome.generation.GeneratorStage;
import biomesoplenty.api.biome.generation.GeneratorWeighted;
import biomesoplenty.common.entities.EntitySnail;
import biomesoplenty.common.enums.BOPClimates;
import biomesoplenty.common.enums.BOPGems;
import biomesoplenty.common.enums.BOPPlants;
import biomesoplenty.common.util.config.BOPConfig.IConfigObj;
import biomesoplenty.common.world.BOPWorldSettings;
import biomesoplenty.common.world.feature.GeneratorFlora;
import biomesoplenty.common.world.feature.GeneratorGrass;
import biomesoplenty.common.world.feature.GeneratorOreSingle;
import biomesoplenty.common.world.feature.tree.GeneratorBigTree;
import biomesoplenty.common.world.feature.tree.GeneratorBush;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase.SpawnListEntry;
import net.minecraft.world.chunk.ChunkPrimer;

public class BiomeGenDenseForest extends BOPBiome
{
    
    public IBlockState usualTopBlock;
    public IBlockState alternateTopBlock;
    
    public BiomeGenDenseForest()
    {
        // terrain
        this.terrainSettings.avgHeight(64).heightVariation(4, 12);
        
        this.setColor(8246897);
        this.setTemperatureRainfall(0.7F, 0.7F);
        
        this.canSpawnInBiome = false;
        this.canGenerateVillages = false;
        
        this.addWeight(BOPClimates.WARM_TEMPERATE, 3);
        
        this.spawnableCreatureList.add(new SpawnListEntry(EntitySnail.class, 6, 1, 2));
        
        this.topBlock = Blocks.grass.getDefaultState();
        this.fillerBlock = Blocks.dirt.getDefaultState();
        this.usualTopBlock = this.topBlock;
        this.alternateTopBlock = Blocks.dirt.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT);
        
        // other plants
        this.addGenerator("bushes", GeneratorStage.FLOWERS, (new GeneratorFlora.Builder()).amountPerChunk(1).with(BOPPlants.BUSH).create());
        this.addGenerator("berry_bushes", GeneratorStage.FLOWERS, (new GeneratorFlora.Builder()).amountPerChunk(1).with(BOPPlants.BERRYBUSH).create());
        this.addGenerator("shrubs", GeneratorStage.FLOWERS, (new GeneratorFlora.Builder()).amountPerChunk(2).with(BOPPlants.SHRUB).create());
        this.addGenerator("water_reeds", GeneratorStage.LILYPAD, (new GeneratorFlora.Builder()).amountPerChunk(2).with(BOPPlants.REED).generationAttempts(32).create());
        this.addGenerator("leaf_piles", GeneratorStage.FLOWERS, (new GeneratorFlora.Builder()).amountPerChunk(5).with(BOPPlants.LEAFPILE).generationAttempts(64).create());        
        this.addGenerator("huge_trees", GeneratorStage.TREE, (new GeneratorBigTree.Builder()).amountPerChunk(7).minHeight(15).maxHeight(30).create());
        this.addGenerator("leaves_clusters", GeneratorStage.POST, (new GeneratorBush.Builder()).amountPerChunk(6).create());

        // grasses
        GeneratorWeighted grassGenerator = new GeneratorWeighted(8);
        this.addGenerator("grass", GeneratorStage.GRASS, grassGenerator);
        grassGenerator.add("shortgrass", 1, (new GeneratorGrass.Builder()).with(BOPPlants.SHORTGRASS).create());
        grassGenerator.add("mediumgrass", 1, (new GeneratorGrass.Builder()).with(BOPPlants.MEDIUMGRASS).create());
        grassGenerator.add("tallgrass", 2, (new GeneratorGrass.Builder()).with(BlockTallGrass.EnumType.GRASS).create());
        grassGenerator.add("fern", 1, (new GeneratorGrass.Builder()).with(BlockTallGrass.EnumType.FERN).create());
        grassGenerator.add("wheatgrass", 1, (new GeneratorGrass.Builder()).with(BOPPlants.WHEATGRASS).create());
        grassGenerator.add("dampgrass", 1, (new GeneratorGrass.Builder()).with(BOPPlants.DAMPGRASS).create());
        
        // gem
        this.addGenerator("amber", GeneratorStage.SAND, (new GeneratorOreSingle.Builder()).amountPerChunk(12).with(BOPGems.AMBER).create());
         
    }
    @Override
    public void applySettings(BOPWorldSettings settings)
    {
        if (!settings.generateBopGems) {this.removeGenerator("amber");}
    }

    @Override
    public void configure(IConfigObj conf)
    {
        super.configure(conf);
        
        this.usualTopBlock = this.topBlock;
        this.alternateTopBlock = conf.getBlockState("alternateTopBlock", this.alternateTopBlock);
    }
    
    @Override
    public void genTerrainBlocks(World world, Random rand, ChunkPrimer primer, int x, int z, double noise)
    {
        this.topBlock = (noise > 1.75D) ? this.alternateTopBlock : this.usualTopBlock;
        super.genTerrainBlocks(world, rand, primer, x, z, noise);
    }
    
    @Override
    public int getGrassColorAtPos(BlockPos pos)
    {
        return 8246897;
    }

    @Override
    public int getFoliageColorAtPos(BlockPos pos)
    {
        return 10022742;
    }
}
