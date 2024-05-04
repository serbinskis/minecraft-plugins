package net.minecraft.stats;

import com.google.common.collect.Sets;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.world.inventory.ContainerRecipeBook;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.crafting.RecipeHolder;

public class RecipeBook {

    public final Set<MinecraftKey> known = Sets.newHashSet();
    protected final Set<MinecraftKey> highlight = Sets.newHashSet();
    private final RecipeBookSettings bookSettings = new RecipeBookSettings();

    public RecipeBook() {}

    public void copyOverData(RecipeBook recipebook) {
        this.known.clear();
        this.highlight.clear();
        this.bookSettings.replaceFrom(recipebook.bookSettings);
        this.known.addAll(recipebook.known);
        this.highlight.addAll(recipebook.highlight);
    }

    public void add(RecipeHolder<?> recipeholder) {
        if (!recipeholder.value().isSpecial()) {
            this.add(recipeholder.id());
        }

    }

    protected void add(MinecraftKey minecraftkey) {
        this.known.add(minecraftkey);
    }

    public boolean contains(@Nullable RecipeHolder<?> recipeholder) {
        return recipeholder == null ? false : this.known.contains(recipeholder.id());
    }

    public boolean contains(MinecraftKey minecraftkey) {
        return this.known.contains(minecraftkey);
    }

    public void remove(RecipeHolder<?> recipeholder) {
        this.remove(recipeholder.id());
    }

    protected void remove(MinecraftKey minecraftkey) {
        this.known.remove(minecraftkey);
        this.highlight.remove(minecraftkey);
    }

    public boolean willHighlight(RecipeHolder<?> recipeholder) {
        return this.highlight.contains(recipeholder.id());
    }

    public void removeHighlight(RecipeHolder<?> recipeholder) {
        this.highlight.remove(recipeholder.id());
    }

    public void addHighlight(RecipeHolder<?> recipeholder) {
        this.addHighlight(recipeholder.id());
    }

    protected void addHighlight(MinecraftKey minecraftkey) {
        this.highlight.add(minecraftkey);
    }

    public boolean isOpen(RecipeBookType recipebooktype) {
        return this.bookSettings.isOpen(recipebooktype);
    }

    public void setOpen(RecipeBookType recipebooktype, boolean flag) {
        this.bookSettings.setOpen(recipebooktype, flag);
    }

    public boolean isFiltering(ContainerRecipeBook<?> containerrecipebook) {
        return this.isFiltering(containerrecipebook.getRecipeBookType());
    }

    public boolean isFiltering(RecipeBookType recipebooktype) {
        return this.bookSettings.isFiltering(recipebooktype);
    }

    public void setFiltering(RecipeBookType recipebooktype, boolean flag) {
        this.bookSettings.setFiltering(recipebooktype, flag);
    }

    public void setBookSettings(RecipeBookSettings recipebooksettings) {
        this.bookSettings.replaceFrom(recipebooksettings);
    }

    public RecipeBookSettings getBookSettings() {
        return this.bookSettings.copy();
    }

    public void setBookSetting(RecipeBookType recipebooktype, boolean flag, boolean flag1) {
        this.bookSettings.setOpen(recipebooktype, flag);
        this.bookSettings.setFiltering(recipebooktype, flag1);
    }
}
