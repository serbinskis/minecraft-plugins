package net.minecraft.commands;

import net.minecraft.network.chat.IChatBaseComponent;

public class FunctionInstantiationException extends Exception {

    private final IChatBaseComponent messageComponent;

    public FunctionInstantiationException(IChatBaseComponent ichatbasecomponent) {
        super(ichatbasecomponent.getString());
        this.messageComponent = ichatbasecomponent;
    }

    public IChatBaseComponent messageComponent() {
        return this.messageComponent;
    }
}
