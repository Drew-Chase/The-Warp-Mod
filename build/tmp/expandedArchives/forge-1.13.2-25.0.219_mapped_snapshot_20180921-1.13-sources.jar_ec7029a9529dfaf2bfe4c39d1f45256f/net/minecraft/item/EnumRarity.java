package net.minecraft.item;

import net.minecraft.util.text.TextFormatting;

public enum EnumRarity implements net.minecraftforge.common.IExtensibleEnum {
   COMMON(TextFormatting.WHITE),
   UNCOMMON(TextFormatting.YELLOW),
   RARE(TextFormatting.AQUA),
   EPIC(TextFormatting.LIGHT_PURPLE);

   /** The color assigned to this rarity type. */
   public final TextFormatting color;

   private EnumRarity(TextFormatting p_i48837_3_) {
      this.color = p_i48837_3_;
   }
   
   public static EnumRarity create(String enumName, TextFormatting p_i48837_3_) {
       throw new IllegalStateException("Enum not extended");
   }
}