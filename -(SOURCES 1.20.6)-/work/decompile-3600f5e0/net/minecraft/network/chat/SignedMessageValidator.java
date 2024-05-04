package net.minecraft.network.chat;

import com.mojang.logging.LogUtils;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import net.minecraft.util.SignatureValidator;
import org.slf4j.Logger;

@FunctionalInterface
public interface SignedMessageValidator {

    Logger LOGGER = LogUtils.getLogger();
    SignedMessageValidator ACCEPT_UNSIGNED = PlayerChatMessage::removeSignature;
    SignedMessageValidator REJECT_ALL = (playerchatmessage) -> {
        SignedMessageValidator.LOGGER.error("Received chat message from {}, but they have no chat session initialized and secure chat is enforced", playerchatmessage.sender());
        return null;
    };

    @Nullable
    PlayerChatMessage updateAndValidate(PlayerChatMessage playerchatmessage);

    public static class a implements SignedMessageValidator {

        private final SignatureValidator validator;
        private final BooleanSupplier expired;
        @Nullable
        private PlayerChatMessage lastMessage;
        private boolean isChainValid = true;

        public a(SignatureValidator signaturevalidator, BooleanSupplier booleansupplier) {
            this.validator = signaturevalidator;
            this.expired = booleansupplier;
        }

        private boolean validateChain(PlayerChatMessage playerchatmessage) {
            if (playerchatmessage.equals(this.lastMessage)) {
                return true;
            } else if (this.lastMessage != null && !playerchatmessage.link().isDescendantOf(this.lastMessage.link())) {
                SignedMessageValidator.a.LOGGER.error("Received out-of-order chat message from {}: expected index > {} for session {}, but was {} for session {}", new Object[]{playerchatmessage.sender(), this.lastMessage.link().index(), this.lastMessage.link().sessionId(), playerchatmessage.link().index(), playerchatmessage.link().sessionId()});
                return false;
            } else {
                return true;
            }
        }

        private boolean validate(PlayerChatMessage playerchatmessage) {
            if (this.expired.getAsBoolean()) {
                SignedMessageValidator.a.LOGGER.error("Received message from player with expired profile public key: {}", playerchatmessage);
                return false;
            } else if (!playerchatmessage.verify(this.validator)) {
                SignedMessageValidator.a.LOGGER.error("Received message with invalid signature from {}", playerchatmessage.sender());
                return false;
            } else {
                return this.validateChain(playerchatmessage);
            }
        }

        @Nullable
        @Override
        public PlayerChatMessage updateAndValidate(PlayerChatMessage playerchatmessage) {
            this.isChainValid = this.isChainValid && this.validate(playerchatmessage);
            if (!this.isChainValid) {
                return null;
            } else {
                this.lastMessage = playerchatmessage;
                return playerchatmessage;
            }
        }
    }
}
