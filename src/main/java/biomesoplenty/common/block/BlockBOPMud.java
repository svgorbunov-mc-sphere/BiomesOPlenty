/*******************************************************************************
 * Copyright 2014-2016, the Biomes O' Plenty Team
 * 
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International Public License.
 * 
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/.
 ******************************************************************************/

package biomesoplenty.common.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import biomesoplenty.api.block.IBOPBlock;
import biomesoplenty.api.block.ISustainsPlantType;
import biomesoplenty.api.item.BOPItems;
import biomesoplenty.common.item.ItemBOPBlock;

public class BlockBOPMud extends Block implements IBOPBlock, ISustainsPlantType
{

    // add properties
    public static enum MudType implements IStringSerializable
    {
        MUD;
        @Override
        public String getName()
        {
            return this.name().toLowerCase();
        }
        @Override
        public String toString()
        {
            return this.getName();
        }
    };
    public static final PropertyEnum VARIANT = PropertyEnum.create("variant", MudType.class);
    @Override
    protected BlockState createBlockState() {return new BlockState(this, new IProperty[] { VARIANT });}
    
    
    // implement IBOPBlock
    @Override
    public Class<? extends ItemBlock> getItemClass() { return ItemBOPBlock.class; }
    @Override
    public int getItemRenderColor(IBlockState state, int tintIndex) { return this.getRenderColor(state); }
    @Override
    public IProperty[] getPresetProperties() { return new IProperty[] {VARIANT}; }
    @Override
    public IProperty[] getNonRenderingProperties() { return null; }
    @Override
    public String getStateName(IBlockState state)
    {
        return ((MudType) state.getValue(VARIANT)).getName();
    }

    public BlockBOPMud() {
        
        // TODO: use a custom material and sount type for mud? A squelching sound?
        super(Material.ground);
        
        // set some defaults
        this.setHardness(0.6F);
        this.setStepSound(Block.soundTypeGrass);
        this.setDefaultState( this.blockState.getBaseState().withProperty(VARIANT, MudType.MUD) );
        
    }    
    
    // map from state to meta and vice verca
    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(VARIANT, MudType.values()[meta]);
    }
    @Override
    public int getMetaFromState(IBlockState state)
    {
        return ((MudType) state.getValue(VARIANT)).ordinal();
    }
    
    
    @Override
    public AxisAlignedBB getCollisionBoundingBox(World world, BlockPos pos, IBlockState state)
    {   
        float heightOffset;
        switch ((MudType) state.getValue(VARIANT))
        {
            // sink a little when standing on mud
            case MUD:
                heightOffset = 0.35F;
                break;
            
            default:
                heightOffset = 0.0F;
                break;
        }
        return new AxisAlignedBB((double) pos.getX(), (double) pos.getY(), (double) pos.getZ(), (double) (pos.getX() + 1), (double) ((float) (pos.getY() + 1) - heightOffset), (double) (pos.getZ() + 1));
    }

    @Override
    public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity)
    {
        
        switch ((MudType) state.getValue(VARIANT))
        {
            // mud slows you greatly unless you're wearing wading boots
            case MUD:
                if (entity instanceof EntityPlayer) {
                    InventoryPlayer inventory = ((EntityPlayer)entity).inventory;
                    if (inventory.armorInventory[0] != null && inventory.armorInventory[0].getItem() == BOPItems.wading_boots) {
                        break;
                    }
                }
                entity.motionX *= 0.1D;
                entity.motionZ *= 0.1D;
                break;
            
            default:
                break;
        }
    }
    
    // drop 4 balls of mud instead of one block
    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune)
    {
        return BOPItems.mudball;
    }

    @Override
    public int quantityDropped(Random random)
    {
        return 4;
    }    
    
    @Override
    public boolean canSustainPlantType(IBlockAccess world, BlockPos pos, EnumPlantType plantType)
    {  
        return false;
    }
    
    
    @Override
    public boolean canSustainPlant(IBlockAccess world, BlockPos pos, EnumFacing direction, net.minecraftforge.common.IPlantable plantable)
    {
        return this.canSustainPlantType(world, pos, plantable.getPlantType(world, pos.offset(direction)));
    }
    
}