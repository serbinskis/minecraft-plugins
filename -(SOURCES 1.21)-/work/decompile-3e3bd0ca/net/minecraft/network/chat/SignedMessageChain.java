package net.minecraft.network.chat;

import com.mojang.logging.LogUtils;
import java.time.Instant;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import net.minecraft.util.SignatureValidator;
import net.minecraft.util.Signer;
import net.minecraft.world.entity.player.ProfilePublicKey;
import org.slf4j.Logger;

public class SignedMessageChain {

    static final Logger LOGGER = LogUtils.getLogger();
    @Nullable
    SignedMessageLink nextLink;
    Instant lastTimeStamp;

    public SignedMessageChain(UUID uuid, UUID uuid1) {
        this.lastTimeStamp = Instant.EPOCH;
        this.nextLink = SignedMessageLink.root(uuid, uuid1);
    }

    public SignedMessageChain.c encoder(Signer signer) {
        return (signedmessagebody) -> {
            SignedMessageLink signedmessagelink = this.nextLink;

            if (signedmessagelink == null) {
                return null;
            } else {
                this.nextLink = signedmessagelink.advance();
                return new MessageSignature(signer.sign((signatureupdater_a) -> {
                    PlayerChatMessage.updateSignature(signatureupdater_a, signedmessagelink, signedmessagebody);
                }));
            }
        };
    }

    public SignedMessageChain.b decoder(final ProfilePublicKey profilepublickey) {
        final SignatureValidator signaturevalidator = profilepublickey.createSignatureValidator();

        return new SignedMessageChain.b() {
            @Override
            public PlayerChatMessage unpack(@Nullable MessageSignature messagesignature, SignedMessageBody signedmessagebody) throws SignedMessageChain.a {
                if (messagesignature == null) {
                    throw new SignedMessageChain.a(SignedMessageChain.a.MISSING_PROFILE_KEY);
                } else if (profilepublickey.data().hasExpired()) {
                    throw new SignedMessageChain.a(SignedMessageChain.a.EXPIRED_PROFILE_KEY);
                } else {
                    SignedMessageLink signedmessagelink = SignedMessageChain.this.nextLink;

                    if (signedmessagelink == null) {
                        throw new SignedMessageChain.a(SignedMessageChain.a.CHAIN_BROKEN);
                    } else if (signedmessagebody.timeStamp().isBefore(SignedMessageChain.this.lastTimeStamp)) {
                        this.setChainBroken();
                        throw new SignedMessageChain.a(SignedMessageChain.a.OUT_OF_ORDER_CHAT);
                    } else {
                        SignedMessageChain.this.lastTimeStamp = signedmessagebody.timeStamp();
                        PlayerChatMessage playerchatmessage = new PlayerChatMessage(signedmessagelink, messagesignature, signedmessagebody, (IChatBaseComponent) null, FilterMask.PASS_THROUGH);

                        if (!playerchatmessage.verify(signaturevalidator)) {
                            this.setChainBroken();
                            throw new SignedMessageChain.a(SignedMessageChain.a.INVALID_SIGNATURE);
                        } else {
                            if (playerchatmessage.hasExpiredServer(Instant.now())) {
                                SignedMessageChain.LOGGER.warn("Received expired chat: '{}'. Is the client/server system time unsynchronized?", signedmessagebody.content());
                            }

                            SignedMessageChain.this.nextLink = signedmessagelink.advance();
                            return playerchatmessage;
                        }
                    }
                }
            }

            @Override
            public void setChainBroken() {
                SignedMessageChain.this.nextLink = null;
            }
        };
    }

    @FunctionalInterface
    public interface c {

        SignedMessageChain.c UNSIGNED = (signedmessagebody) -> {
            return null;
        };

        @Nullable
        MessageSignature pack(SignedMessageBody signedmessagebody);
    }

    public static class a extends ThrowingComponent {

        static final IChatBaseComponent MISSING_PROFILE_KEY = IChatBaseComponent.translatable("chat.disabled.missingProfileKey");
        static final IChatBaseComponent CHAIN_BROKEN = IChatBaseComponent.translatable("chat.disabled.chain_broken");
        static final IChatBaseComponent EXPIRED_PROFILE_KEY = IChatBaseComponent.translatable("chat.disabled.expiredProfileKey");
        static final IChatBaseComponent INVALID_SIGNATURE = IChatBaseComponent.translatable("chat.disabled.invalid_signature");
        static final IChatBaseComponent OUT_OF_ORDER_CHAT = IChatBaseComponent.translatable("chat.disabled.out_of_order_chat");

        public a(IChatBaseComponent ichatbasecomponent) {
            super(ichatbasecomponent);
        }
    }

    @FunctionalInterface
    public interface b {

        static SignedMessageChain.b unsigned(UUID uuid, BooleanSupplier booleansupplier) {
            return (messagesignature, signedmessagebody) -> {
                if (booleansupplier.getAsBoolean()) {
                    throw new SignedMessageChain.a(SignedMessageChain.a.MISSING_PROFILE_KEY);
                } else {
                    return PlayerChatMessage.unsigned(uuid, signedmessagebody.content());
                }
            };
        }

        PlayerChatMessage unpack(@Nullable MessageSignature messagesignature, SignedMessageBody signedmessagebody) throws SignedMessageChain.a;

        default void setChainBroken() {}
    }
}
