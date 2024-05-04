package net.minecraft.world.item.trading;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.MathHelper;
import net.minecraft.world.item.ItemStack;

public class MerchantRecipe {

    public static final Codec<MerchantRecipe> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(ItemCost.CODEC.fieldOf("buy").forGetter((merchantrecipe) -> {
            return merchantrecipe.baseCostA;
        }), ItemCost.CODEC.lenientOptionalFieldOf("buyB").forGetter((merchantrecipe) -> {
            return merchantrecipe.costB;
        }), ItemStack.CODEC.fieldOf("sell").forGetter((merchantrecipe) -> {
            return merchantrecipe.result;
        }), Codec.INT.lenientOptionalFieldOf("uses", 0).forGetter((merchantrecipe) -> {
            return merchantrecipe.uses;
        }), Codec.INT.lenientOptionalFieldOf("maxUses", 4).forGetter((merchantrecipe) -> {
            return merchantrecipe.maxUses;
        }), Codec.BOOL.lenientOptionalFieldOf("rewardExp", true).forGetter((merchantrecipe) -> {
            return merchantrecipe.rewardExp;
        }), Codec.INT.lenientOptionalFieldOf("specialPrice", 0).forGetter((merchantrecipe) -> {
            return merchantrecipe.specialPriceDiff;
        }), Codec.INT.lenientOptionalFieldOf("demand", 0).forGetter((merchantrecipe) -> {
            return merchantrecipe.demand;
        }), Codec.FLOAT.lenientOptionalFieldOf("priceMultiplier", 0.0F).forGetter((merchantrecipe) -> {
            return merchantrecipe.priceMultiplier;
        }), Codec.INT.lenientOptionalFieldOf("xp", 1).forGetter((merchantrecipe) -> {
            return merchantrecipe.xp;
        })).apply(instance, MerchantRecipe::new);
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, MerchantRecipe> STREAM_CODEC = StreamCodec.of(MerchantRecipe::writeToStream, MerchantRecipe::createFromStream);
    public ItemCost baseCostA;
    public Optional<ItemCost> costB;
    public final ItemStack result;
    public int uses;
    public int maxUses;
    public boolean rewardExp;
    public int specialPriceDiff;
    public int demand;
    public float priceMultiplier;
    public int xp;

    private MerchantRecipe(ItemCost itemcost, Optional<ItemCost> optional, ItemStack itemstack, int i, int j, boolean flag, int k, int l, float f, int i1) {
        this.baseCostA = itemcost;
        this.costB = optional;
        this.result = itemstack;
        this.uses = i;
        this.maxUses = j;
        this.rewardExp = flag;
        this.specialPriceDiff = k;
        this.demand = l;
        this.priceMultiplier = f;
        this.xp = i1;
    }

    public MerchantRecipe(ItemCost itemcost, ItemStack itemstack, int i, int j, float f) {
        this(itemcost, Optional.empty(), itemstack, i, j, f);
    }

    public MerchantRecipe(ItemCost itemcost, Optional<ItemCost> optional, ItemStack itemstack, int i, int j, float f) {
        this(itemcost, optional, itemstack, 0, i, j, f);
    }

    public MerchantRecipe(ItemCost itemcost, Optional<ItemCost> optional, ItemStack itemstack, int i, int j, int k, float f) {
        this(itemcost, optional, itemstack, i, j, k, f, 0);
    }

    public MerchantRecipe(ItemCost itemcost, Optional<ItemCost> optional, ItemStack itemstack, int i, int j, int k, float f, int l) {
        this(itemcost, optional, itemstack, i, j, true, 0, l, f, k);
    }

    private MerchantRecipe(MerchantRecipe merchantrecipe) {
        this(merchantrecipe.baseCostA, merchantrecipe.costB, merchantrecipe.result.copy(), merchantrecipe.uses, merchantrecipe.maxUses, merchantrecipe.rewardExp, merchantrecipe.specialPriceDiff, merchantrecipe.demand, merchantrecipe.priceMultiplier, merchantrecipe.xp);
    }

    public ItemStack getBaseCostA() {
        return this.baseCostA.itemStack();
    }

    public ItemStack getCostA() {
        return this.baseCostA.itemStack().copyWithCount(this.getModifiedCostCount(this.baseCostA));
    }

    private int getModifiedCostCount(ItemCost itemcost) {
        int i = itemcost.count();
        int j = Math.max(0, MathHelper.floor((float) (i * this.demand) * this.priceMultiplier));

        return MathHelper.clamp(i + j + this.specialPriceDiff, 1, itemcost.itemStack().getMaxStackSize());
    }

    public ItemStack getCostB() {
        return (ItemStack) this.costB.map(ItemCost::itemStack).orElse(ItemStack.EMPTY);
    }

    public ItemCost getItemCostA() {
        return this.baseCostA;
    }

    public Optional<ItemCost> getItemCostB() {
        return this.costB;
    }

    public ItemStack getResult() {
        return this.result;
    }

    public void updateDemand() {
        this.demand = this.demand + this.uses - (this.maxUses - this.uses);
    }

    public ItemStack assemble() {
        return this.result.copy();
    }

    public int getUses() {
        return this.uses;
    }

    public void resetUses() {
        this.uses = 0;
    }

    public int getMaxUses() {
        return this.maxUses;
    }

    public void increaseUses() {
        ++this.uses;
    }

    public int getDemand() {
        return this.demand;
    }

    public void addToSpecialPriceDiff(int i) {
        this.specialPriceDiff += i;
    }

    public void resetSpecialPriceDiff() {
        this.specialPriceDiff = 0;
    }

    public int getSpecialPriceDiff() {
        return this.specialPriceDiff;
    }

    public void setSpecialPriceDiff(int i) {
        this.specialPriceDiff = i;
    }

    public float getPriceMultiplier() {
        return this.priceMultiplier;
    }

    public int getXp() {
        return this.xp;
    }

    public boolean isOutOfStock() {
        return this.uses >= this.maxUses;
    }

    public void setToOutOfStock() {
        this.uses = this.maxUses;
    }

    public boolean needsRestock() {
        return this.uses > 0;
    }

    public boolean shouldRewardExp() {
        return this.rewardExp;
    }

    public boolean satisfiedBy(ItemStack itemstack, ItemStack itemstack1) {
        return this.baseCostA.test(itemstack) && itemstack.getCount() >= this.getModifiedCostCount(this.baseCostA) ? (!this.costB.isPresent() ? itemstack1.isEmpty() : ((ItemCost) this.costB.get()).test(itemstack1) && itemstack1.getCount() >= ((ItemCost) this.costB.get()).count()) : false;
    }

    public boolean take(ItemStack itemstack, ItemStack itemstack1) {
        if (!this.satisfiedBy(itemstack, itemstack1)) {
            return false;
        } else {
            itemstack.shrink(this.getCostA().getCount());
            if (!this.getCostB().isEmpty()) {
                itemstack1.shrink(this.getCostB().getCount());
            }

            return true;
        }
    }

    public MerchantRecipe copy() {
        return new MerchantRecipe(this);
    }

    private static void writeToStream(RegistryFriendlyByteBuf registryfriendlybytebuf, MerchantRecipe merchantrecipe) {
        ItemCost.STREAM_CODEC.encode(registryfriendlybytebuf, merchantrecipe.getItemCostA());
        ItemStack.STREAM_CODEC.encode(registryfriendlybytebuf, merchantrecipe.getResult());
        ItemCost.OPTIONAL_STREAM_CODEC.encode(registryfriendlybytebuf, merchantrecipe.getItemCostB());
        registryfriendlybytebuf.writeBoolean(merchantrecipe.isOutOfStock());
        registryfriendlybytebuf.writeInt(merchantrecipe.getUses());
        registryfriendlybytebuf.writeInt(merchantrecipe.getMaxUses());
        registryfriendlybytebuf.writeInt(merchantrecipe.getXp());
        registryfriendlybytebuf.writeInt(merchantrecipe.getSpecialPriceDiff());
        registryfriendlybytebuf.writeFloat(merchantrecipe.getPriceMultiplier());
        registryfriendlybytebuf.writeInt(merchantrecipe.getDemand());
    }

    public static MerchantRecipe createFromStream(RegistryFriendlyByteBuf registryfriendlybytebuf) {
        ItemCost itemcost = (ItemCost) ItemCost.STREAM_CODEC.decode(registryfriendlybytebuf);
        ItemStack itemstack = (ItemStack) ItemStack.STREAM_CODEC.decode(registryfriendlybytebuf);
        Optional<ItemCost> optional = (Optional) ItemCost.OPTIONAL_STREAM_CODEC.decode(registryfriendlybytebuf);
        boolean flag = registryfriendlybytebuf.readBoolean();
        int i = registryfriendlybytebuf.readInt();
        int j = registryfriendlybytebuf.readInt();
        int k = registryfriendlybytebuf.readInt();
        int l = registryfriendlybytebuf.readInt();
        float f = registryfriendlybytebuf.readFloat();
        int i1 = registryfriendlybytebuf.readInt();
        MerchantRecipe merchantrecipe = new MerchantRecipe(itemcost, optional, itemstack, i, j, k, f, i1);

        if (flag) {
            merchantrecipe.setToOutOfStock();
        }

        merchantrecipe.setSpecialPriceDiff(l);
        return merchantrecipe;
    }
}
