package net.minecraft.world.item;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.Holder;
import net.minecraft.core.dispenser.DispenseBehaviorItem;
import net.minecraft.core.dispenser.IDispenseBehavior;
import net.minecraft.core.dispenser.SourceBlock;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.util.INamable;
import net.minecraft.world.EnumHand;
import net.minecraft.world.InteractionResultWrapper;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.IEntitySelector;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.crafting.RecipeItemStack;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.BlockDispenser;
import net.minecraft.world.phys.AxisAlignedBB;

public class ItemArmor extends Item implements Equipable {

    public static final IDispenseBehavior DISPENSE_ITEM_BEHAVIOR = new DispenseBehaviorItem() {
        @Override
        protected ItemStack execute(SourceBlock sourceblock, ItemStack itemstack) {
            return ItemArmor.dispenseArmor(sourceblock, itemstack) ? itemstack : super.execute(sourceblock, itemstack);
        }
    };
    protected final ItemArmor.a type;
    protected final Holder<ArmorMaterial> material;
    private final Supplier<ItemAttributeModifiers> defaultModifiers;

    public static boolean dispenseArmor(SourceBlock sourceblock, ItemStack itemstack) {
        BlockPosition blockposition = sourceblock.pos().relative((EnumDirection) sourceblock.state().getValue(BlockDispenser.FACING));
        List<EntityLiving> list = sourceblock.level().getEntitiesOfClass(EntityLiving.class, new AxisAlignedBB(blockposition), IEntitySelector.NO_SPECTATORS.and(new IEntitySelector.EntitySelectorEquipable(itemstack)));

        if (list.isEmpty()) {
            return false;
        } else {
            EntityLiving entityliving = (EntityLiving) list.get(0);
            EnumItemSlot enumitemslot = entityliving.getEquipmentSlotForItem(itemstack);
            ItemStack itemstack1 = itemstack.split(1);

            entityliving.setItemSlot(enumitemslot, itemstack1);
            if (entityliving instanceof EntityInsentient) {
                ((EntityInsentient) entityliving).setDropChance(enumitemslot, 2.0F);
                ((EntityInsentient) entityliving).setPersistenceRequired();
            }

            return true;
        }
    }

    public ItemArmor(Holder<ArmorMaterial> holder, ItemArmor.a itemarmor_a, Item.Info item_info) {
        super(item_info);
        this.material = holder;
        this.type = itemarmor_a;
        BlockDispenser.registerBehavior(this, ItemArmor.DISPENSE_ITEM_BEHAVIOR);
        this.defaultModifiers = Suppliers.memoize(() -> {
            int i = ((ArmorMaterial) holder.value()).getDefense(itemarmor_a);
            float f = ((ArmorMaterial) holder.value()).toughness();
            ItemAttributeModifiers.a itemattributemodifiers_a = ItemAttributeModifiers.builder();
            EquipmentSlotGroup equipmentslotgroup = EquipmentSlotGroup.bySlot(itemarmor_a.getSlot());
            MinecraftKey minecraftkey = MinecraftKey.withDefaultNamespace("armor." + itemarmor_a.getName());

            itemattributemodifiers_a.add(GenericAttributes.ARMOR, new AttributeModifier(minecraftkey, (double) i, AttributeModifier.Operation.ADD_VALUE), equipmentslotgroup);
            itemattributemodifiers_a.add(GenericAttributes.ARMOR_TOUGHNESS, new AttributeModifier(minecraftkey, (double) f, AttributeModifier.Operation.ADD_VALUE), equipmentslotgroup);
            float f1 = ((ArmorMaterial) holder.value()).knockbackResistance();

            if (f1 > 0.0F) {
                itemattributemodifiers_a.add(GenericAttributes.KNOCKBACK_RESISTANCE, new AttributeModifier(minecraftkey, (double) f1, AttributeModifier.Operation.ADD_VALUE), equipmentslotgroup);
            }

            return itemattributemodifiers_a.build();
        });
    }

    public ItemArmor.a getType() {
        return this.type;
    }

    @Override
    public int getEnchantmentValue() {
        return ((ArmorMaterial) this.material.value()).enchantmentValue();
    }

    public Holder<ArmorMaterial> getMaterial() {
        return this.material;
    }

    @Override
    public boolean isValidRepairItem(ItemStack itemstack, ItemStack itemstack1) {
        return ((RecipeItemStack) ((ArmorMaterial) this.material.value()).repairIngredient().get()).test(itemstack1) || super.isValidRepairItem(itemstack, itemstack1);
    }

    @Override
    public InteractionResultWrapper<ItemStack> use(World world, EntityHuman entityhuman, EnumHand enumhand) {
        return this.swapWithEquipmentSlot(this, world, entityhuman, enumhand);
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers() {
        return (ItemAttributeModifiers) this.defaultModifiers.get();
    }

    public int getDefense() {
        return ((ArmorMaterial) this.material.value()).getDefense(this.type);
    }

    public float getToughness() {
        return ((ArmorMaterial) this.material.value()).toughness();
    }

    @Override
    public EnumItemSlot getEquipmentSlot() {
        return this.type.getSlot();
    }

    @Override
    public Holder<SoundEffect> getEquipSound() {
        return ((ArmorMaterial) this.getMaterial().value()).equipSound();
    }

    public static enum a implements INamable {

        HELMET(EnumItemSlot.HEAD, 11, "helmet"), CHESTPLATE(EnumItemSlot.CHEST, 16, "chestplate"), LEGGINGS(EnumItemSlot.LEGS, 15, "leggings"), BOOTS(EnumItemSlot.FEET, 13, "boots"), BODY(EnumItemSlot.BODY, 16, "body");

        public static final Codec<ItemArmor.a> CODEC = INamable.fromValues(ItemArmor.a::values);
        private final EnumItemSlot slot;
        private final String name;
        private final int durability;

        private a(final EnumItemSlot enumitemslot, final int i, final String s) {
            this.slot = enumitemslot;
            this.name = s;
            this.durability = i;
        }

        public int getDurability(int i) {
            return this.durability * i;
        }

        public EnumItemSlot getSlot() {
            return this.slot;
        }

        public String getName() {
            return this.name;
        }

        public boolean hasTrims() {
            return this == ItemArmor.a.HELMET || this == ItemArmor.a.CHESTPLATE || this == ItemArmor.a.LEGGINGS || this == ItemArmor.a.BOOTS;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
