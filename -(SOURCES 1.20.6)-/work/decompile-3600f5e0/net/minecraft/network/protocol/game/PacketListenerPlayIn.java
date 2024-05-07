package net.minecraft.network.protocol.game;

import net.minecraft.network.EnumProtocol;
import net.minecraft.network.protocol.common.ServerCommonPacketListener;
import net.minecraft.network.protocol.ping.ServerPingPacketListener;

public interface PacketListenerPlayIn extends ServerCommonPacketListener, ServerPingPacketListener {

    @Override
    default EnumProtocol protocol() {
        return EnumProtocol.PLAY;
    }

    void handleAnimate(PacketPlayInArmAnimation packetplayinarmanimation);

    void handleChat(PacketPlayInChat packetplayinchat);

    void handleChatCommand(ServerboundChatCommandPacket serverboundchatcommandpacket);

    void handleSignedChatCommand(ServerboundChatCommandSignedPacket serverboundchatcommandsignedpacket);

    void handleChatAck(ServerboundChatAckPacket serverboundchatackpacket);

    void handleClientCommand(PacketPlayInClientCommand packetplayinclientcommand);

    void handleContainerButtonClick(PacketPlayInEnchantItem packetplayinenchantitem);

    void handleContainerClick(PacketPlayInWindowClick packetplayinwindowclick);

    void handlePlaceRecipe(PacketPlayInAutoRecipe packetplayinautorecipe);

    void handleContainerClose(PacketPlayInCloseWindow packetplayinclosewindow);

    void handleInteract(PacketPlayInUseEntity packetplayinuseentity);

    void handleMovePlayer(PacketPlayInFlying packetplayinflying);

    void handlePlayerAbilities(PacketPlayInAbilities packetplayinabilities);

    void handlePlayerAction(PacketPlayInBlockDig packetplayinblockdig);

    void handlePlayerCommand(PacketPlayInEntityAction packetplayinentityaction);

    void handlePlayerInput(PacketPlayInSteerVehicle packetplayinsteervehicle);

    void handleSetCarriedItem(PacketPlayInHeldItemSlot packetplayinhelditemslot);

    void handleSetCreativeModeSlot(PacketPlayInSetCreativeSlot packetplayinsetcreativeslot);

    void handleSignUpdate(PacketPlayInUpdateSign packetplayinupdatesign);

    void handleUseItemOn(PacketPlayInUseItem packetplayinuseitem);

    void handleUseItem(PacketPlayInBlockPlace packetplayinblockplace);

    void handleTeleportToEntityPacket(PacketPlayInSpectate packetplayinspectate);

    void handlePaddleBoat(PacketPlayInBoatMove packetplayinboatmove);

    void handleMoveVehicle(PacketPlayInVehicleMove packetplayinvehiclemove);

    void handleAcceptTeleportPacket(PacketPlayInTeleportAccept packetplayinteleportaccept);

    void handleRecipeBookSeenRecipePacket(PacketPlayInRecipeDisplayed packetplayinrecipedisplayed);

    void handleRecipeBookChangeSettingsPacket(PacketPlayInRecipeSettings packetplayinrecipesettings);

    void handleSeenAdvancements(PacketPlayInAdvancements packetplayinadvancements);

    void handleCustomCommandSuggestions(PacketPlayInTabComplete packetplayintabcomplete);

    void handleSetCommandBlock(PacketPlayInSetCommandBlock packetplayinsetcommandblock);

    void handleSetCommandMinecart(PacketPlayInSetCommandMinecart packetplayinsetcommandminecart);

    void handlePickItem(PacketPlayInPickItem packetplayinpickitem);

    void handleRenameItem(PacketPlayInItemName packetplayinitemname);

    void handleSetBeaconPacket(PacketPlayInBeacon packetplayinbeacon);

    void handleSetStructureBlock(PacketPlayInStruct packetplayinstruct);

    void handleSelectTrade(PacketPlayInTrSel packetplayintrsel);

    void handleEditBook(PacketPlayInBEdit packetplayinbedit);

    void handleEntityTagQuery(PacketPlayInEntityNBTQuery packetplayinentitynbtquery);

    void handleContainerSlotStateChanged(ServerboundContainerSlotStateChangedPacket serverboundcontainerslotstatechangedpacket);

    void handleBlockEntityTagQuery(PacketPlayInTileNBTQuery packetplayintilenbtquery);

    void handleSetJigsawBlock(PacketPlayInSetJigsaw packetplayinsetjigsaw);

    void handleJigsawGenerate(PacketPlayInJigsawGenerate packetplayinjigsawgenerate);

    void handleChangeDifficulty(PacketPlayInDifficultyChange packetplayindifficultychange);

    void handleLockDifficulty(PacketPlayInDifficultyLock packetplayindifficultylock);

    void handleChatSessionUpdate(ServerboundChatSessionUpdatePacket serverboundchatsessionupdatepacket);

    void handleConfigurationAcknowledged(ServerboundConfigurationAcknowledgedPacket serverboundconfigurationacknowledgedpacket);

    void handleChunkBatchReceived(ServerboundChunkBatchReceivedPacket serverboundchunkbatchreceivedpacket);

    void handleDebugSampleSubscription(ServerboundDebugSampleSubscriptionPacket serverbounddebugsamplesubscriptionpacket);
}
