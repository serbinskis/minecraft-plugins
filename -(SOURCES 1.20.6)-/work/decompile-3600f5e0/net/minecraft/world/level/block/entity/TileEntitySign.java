package net.minecraft.world.level.block.entity;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.ICommandListener;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.chat.ChatClickable;
import net.minecraft.network.chat.ChatComponentUtils;
import net.minecraft.network.chat.ChatModifier;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutTileEntityData;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.network.FilteredText;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.util.MathHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BlockSign;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.phys.Vec2F;
import net.minecraft.world.phys.Vec3D;
import org.slf4j.Logger;

public class TileEntitySign extends TileEntity {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_TEXT_LINE_WIDTH = 90;
    private static final int TEXT_LINE_HEIGHT = 10;
    @Nullable
    public UUID playerWhoMayEdit;
    private SignText frontText;
    private SignText backText;
    private boolean isWaxed;

    public TileEntitySign(BlockPosition blockposition, IBlockData iblockdata) {
        this(TileEntityTypes.SIGN, blockposition, iblockdata);
    }

    public TileEntitySign(TileEntityTypes tileentitytypes, BlockPosition blockposition, IBlockData iblockdata) {
        super(tileentitytypes, blockposition, iblockdata);
        this.frontText = this.createDefaultSignText();
        this.backText = this.createDefaultSignText();
    }

    protected SignText createDefaultSignText() {
        return new SignText();
    }

    public boolean isFacingFrontText(EntityHuman entityhuman) {
        Block block = this.getBlockState().getBlock();

        if (block instanceof BlockSign blocksign) {
            Vec3D vec3d = blocksign.getSignHitboxCenterPosition(this.getBlockState());
            double d0 = entityhuman.getX() - ((double) this.getBlockPos().getX() + vec3d.x);
            double d1 = entityhuman.getZ() - ((double) this.getBlockPos().getZ() + vec3d.z);
            float f = blocksign.getYRotationDegrees(this.getBlockState());
            float f1 = (float) (MathHelper.atan2(d1, d0) * 57.2957763671875D) - 90.0F;

            return MathHelper.degreesDifferenceAbs(f, f1) <= 90.0F;
        } else {
            return false;
        }
    }

    public SignText getText(boolean flag) {
        return flag ? this.frontText : this.backText;
    }

    public SignText getFrontText() {
        return this.frontText;
    }

    public SignText getBackText() {
        return this.backText;
    }

    public int getTextLineHeight() {
        return 10;
    }

    public int getMaxTextLineWidth() {
        return 90;
    }

    @Override
    protected void saveAdditional(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        super.saveAdditional(nbttagcompound, holderlookup_a);
        DynamicOps<NBTBase> dynamicops = holderlookup_a.createSerializationContext(DynamicOpsNBT.INSTANCE);
        DataResult dataresult = SignText.DIRECT_CODEC.encodeStart(dynamicops, this.frontText);
        Logger logger = TileEntitySign.LOGGER;

        Objects.requireNonNull(logger);
        dataresult.resultOrPartial(logger::error).ifPresent((nbtbase) -> {
            nbttagcompound.put("front_text", nbtbase);
        });
        dataresult = SignText.DIRECT_CODEC.encodeStart(dynamicops, this.backText);
        logger = TileEntitySign.LOGGER;
        Objects.requireNonNull(logger);
        dataresult.resultOrPartial(logger::error).ifPresent((nbtbase) -> {
            nbttagcompound.put("back_text", nbtbase);
        });
        nbttagcompound.putBoolean("is_waxed", this.isWaxed);
    }

    @Override
    protected void loadAdditional(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        super.loadAdditional(nbttagcompound, holderlookup_a);
        DynamicOps<NBTBase> dynamicops = holderlookup_a.createSerializationContext(DynamicOpsNBT.INSTANCE);
        DataResult dataresult;
        Logger logger;

        if (nbttagcompound.contains("front_text")) {
            dataresult = SignText.DIRECT_CODEC.parse(dynamicops, nbttagcompound.getCompound("front_text"));
            logger = TileEntitySign.LOGGER;
            Objects.requireNonNull(logger);
            dataresult.resultOrPartial(logger::error).ifPresent((signtext) -> {
                this.frontText = this.loadLines(signtext);
            });
        }

        if (nbttagcompound.contains("back_text")) {
            dataresult = SignText.DIRECT_CODEC.parse(dynamicops, nbttagcompound.getCompound("back_text"));
            logger = TileEntitySign.LOGGER;
            Objects.requireNonNull(logger);
            dataresult.resultOrPartial(logger::error).ifPresent((signtext) -> {
                this.backText = this.loadLines(signtext);
            });
        }

        this.isWaxed = nbttagcompound.getBoolean("is_waxed");
    }

    private SignText loadLines(SignText signtext) {
        for (int i = 0; i < 4; ++i) {
            IChatBaseComponent ichatbasecomponent = this.loadLine(signtext.getMessage(i, false));
            IChatBaseComponent ichatbasecomponent1 = this.loadLine(signtext.getMessage(i, true));

            signtext = signtext.setMessage(i, ichatbasecomponent, ichatbasecomponent1);
        }

        return signtext;
    }

    private IChatBaseComponent loadLine(IChatBaseComponent ichatbasecomponent) {
        World world = this.level;

        if (world instanceof WorldServer worldserver) {
            try {
                return ChatComponentUtils.updateForEntity(createCommandSourceStack((EntityHuman) null, worldserver, this.worldPosition), ichatbasecomponent, (Entity) null, 0);
            } catch (CommandSyntaxException commandsyntaxexception) {
                ;
            }
        }

        return ichatbasecomponent;
    }

    public void updateSignText(EntityHuman entityhuman, boolean flag, List<FilteredText> list) {
        if (!this.isWaxed() && entityhuman.getUUID().equals(this.getPlayerWhoMayEdit()) && this.level != null) {
            this.updateText((signtext) -> {
                return this.setMessages(entityhuman, list, signtext);
            }, flag);
            this.setAllowedPlayerEditor((UUID) null);
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        } else {
            TileEntitySign.LOGGER.warn("Player {} just tried to change non-editable sign", entityhuman.getName().getString());
        }
    }

    public boolean updateText(UnaryOperator<SignText> unaryoperator, boolean flag) {
        SignText signtext = this.getText(flag);

        return this.setText((SignText) unaryoperator.apply(signtext), flag);
    }

    private SignText setMessages(EntityHuman entityhuman, List<FilteredText> list, SignText signtext) {
        for (int i = 0; i < list.size(); ++i) {
            FilteredText filteredtext = (FilteredText) list.get(i);
            ChatModifier chatmodifier = signtext.getMessage(i, entityhuman.isTextFilteringEnabled()).getStyle();

            if (entityhuman.isTextFilteringEnabled()) {
                signtext = signtext.setMessage(i, IChatBaseComponent.literal(filteredtext.filteredOrEmpty()).setStyle(chatmodifier));
            } else {
                signtext = signtext.setMessage(i, IChatBaseComponent.literal(filteredtext.raw()).setStyle(chatmodifier), IChatBaseComponent.literal(filteredtext.filteredOrEmpty()).setStyle(chatmodifier));
            }
        }

        return signtext;
    }

    public boolean setText(SignText signtext, boolean flag) {
        return flag ? this.setFrontText(signtext) : this.setBackText(signtext);
    }

    private boolean setBackText(SignText signtext) {
        if (signtext != this.backText) {
            this.backText = signtext;
            this.markUpdated();
            return true;
        } else {
            return false;
        }
    }

    private boolean setFrontText(SignText signtext) {
        if (signtext != this.frontText) {
            this.frontText = signtext;
            this.markUpdated();
            return true;
        } else {
            return false;
        }
    }

    public boolean canExecuteClickCommands(boolean flag, EntityHuman entityhuman) {
        return this.isWaxed() && this.getText(flag).hasAnyClickCommands(entityhuman);
    }

    public boolean executeClickCommandsIfPresent(EntityHuman entityhuman, World world, BlockPosition blockposition, boolean flag) {
        boolean flag1 = false;
        IChatBaseComponent[] aichatbasecomponent = this.getText(flag).getMessages(entityhuman.isTextFilteringEnabled());
        int i = aichatbasecomponent.length;

        for (int j = 0; j < i; ++j) {
            IChatBaseComponent ichatbasecomponent = aichatbasecomponent[j];
            ChatModifier chatmodifier = ichatbasecomponent.getStyle();
            ChatClickable chatclickable = chatmodifier.getClickEvent();

            if (chatclickable != null && chatclickable.getAction() == ChatClickable.EnumClickAction.RUN_COMMAND) {
                entityhuman.getServer().getCommands().performPrefixedCommand(createCommandSourceStack(entityhuman, world, blockposition), chatclickable.getValue());
                flag1 = true;
            }
        }

        return flag1;
    }

    private static CommandListenerWrapper createCommandSourceStack(@Nullable EntityHuman entityhuman, World world, BlockPosition blockposition) {
        String s = entityhuman == null ? "Sign" : entityhuman.getName().getString();
        Object object = entityhuman == null ? IChatBaseComponent.literal("Sign") : entityhuman.getDisplayName();

        return new CommandListenerWrapper(ICommandListener.NULL, Vec3D.atCenterOf(blockposition), Vec2F.ZERO, (WorldServer) world, 2, s, (IChatBaseComponent) object, world.getServer(), entityhuman);
    }

    @Override
    public PacketPlayOutTileEntityData getUpdatePacket() {
        return PacketPlayOutTileEntityData.create(this);
    }

    @Override
    public NBTTagCompound getUpdateTag(HolderLookup.a holderlookup_a) {
        return this.saveCustomOnly(holderlookup_a);
    }

    @Override
    public boolean onlyOpCanSetNbt() {
        return true;
    }

    public void setAllowedPlayerEditor(@Nullable UUID uuid) {
        this.playerWhoMayEdit = uuid;
    }

    @Nullable
    public UUID getPlayerWhoMayEdit() {
        return this.playerWhoMayEdit;
    }

    private void markUpdated() {
        this.setChanged();
        this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

    public boolean isWaxed() {
        return this.isWaxed;
    }

    public boolean setWaxed(boolean flag) {
        if (this.isWaxed != flag) {
            this.isWaxed = flag;
            this.markUpdated();
            return true;
        } else {
            return false;
        }
    }

    public boolean playerIsTooFarAwayToEdit(UUID uuid) {
        EntityHuman entityhuman = this.level.getPlayerByUUID(uuid);

        return entityhuman == null || !entityhuman.canInteractWithBlock(this.getBlockPos(), 4.0D);
    }

    public static void tick(World world, BlockPosition blockposition, IBlockData iblockdata, TileEntitySign tileentitysign) {
        UUID uuid = tileentitysign.getPlayerWhoMayEdit();

        if (uuid != null) {
            tileentitysign.clearInvalidPlayerWhoMayEdit(tileentitysign, world, uuid);
        }

    }

    private void clearInvalidPlayerWhoMayEdit(TileEntitySign tileentitysign, World world, UUID uuid) {
        if (tileentitysign.playerIsTooFarAwayToEdit(uuid)) {
            tileentitysign.setAllowedPlayerEditor((UUID) null);
        }

    }

    public SoundEffect getSignInteractionFailedSoundEvent() {
        return SoundEffects.WAXED_SIGN_INTERACT_FAIL;
    }
}
