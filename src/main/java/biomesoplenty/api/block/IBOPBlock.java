/*******************************************************************************
 * Copyright 2014, the Biomes O' Plenty Team
 * 
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International Public License.
 * 
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/.
 ******************************************************************************/

package biomesoplenty.api.block;

import java.util.Map;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;

public interface IBOPBlock {
    
    public Map<String, IBlockState> getNamedStates();
    public IBlockState getNamedState(String name);
    public Class<? extends ItemBlock> getItemClass();
    
}