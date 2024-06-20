package net.minecraft.nbt;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.bytes.ByteCollection;
import it.unimi.dsi.fastutil.bytes.ByteOpenHashSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.EnumChatFormat;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;
import org.slf4j.Logger;

public class TextComponentTagVisitor implements TagVisitor {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int INLINE_LIST_THRESHOLD = 8;
    private static final int MAX_DEPTH = 64;
    private static final int MAX_LENGTH = 128;
    private static final ByteCollection INLINE_ELEMENT_TYPES = new ByteOpenHashSet(Arrays.asList(1, 2, 3, 4, 5, 6));
    private static final EnumChatFormat SYNTAX_HIGHLIGHTING_KEY = EnumChatFormat.AQUA;
    private static final EnumChatFormat SYNTAX_HIGHLIGHTING_STRING = EnumChatFormat.GREEN;
    private static final EnumChatFormat SYNTAX_HIGHLIGHTING_NUMBER = EnumChatFormat.GOLD;
    private static final EnumChatFormat SYNTAX_HIGHLIGHTING_NUMBER_TYPE = EnumChatFormat.RED;
    private static final Pattern SIMPLE_VALUE = Pattern.compile("[A-Za-z0-9._+-]+");
    private static final String LIST_OPEN = "[";
    private static final String LIST_CLOSE = "]";
    private static final String LIST_TYPE_SEPARATOR = ";";
    private static final String ELEMENT_SPACING = " ";
    private static final String STRUCT_OPEN = "{";
    private static final String STRUCT_CLOSE = "}";
    private static final String NEWLINE = "\n";
    private static final String NAME_VALUE_SEPARATOR = ": ";
    private static final String ELEMENT_SEPARATOR = String.valueOf(',');
    private static final String WRAPPED_ELEMENT_SEPARATOR = TextComponentTagVisitor.ELEMENT_SEPARATOR + "\n";
    private static final String SPACED_ELEMENT_SEPARATOR = TextComponentTagVisitor.ELEMENT_SEPARATOR + " ";
    private static final IChatBaseComponent FOLDED = IChatBaseComponent.literal("<...>").withStyle(EnumChatFormat.GRAY);
    private static final IChatBaseComponent BYTE_TYPE = IChatBaseComponent.literal("b").withStyle(TextComponentTagVisitor.SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
    private static final IChatBaseComponent SHORT_TYPE = IChatBaseComponent.literal("s").withStyle(TextComponentTagVisitor.SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
    private static final IChatBaseComponent INT_TYPE = IChatBaseComponent.literal("I").withStyle(TextComponentTagVisitor.SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
    private static final IChatBaseComponent LONG_TYPE = IChatBaseComponent.literal("L").withStyle(TextComponentTagVisitor.SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
    private static final IChatBaseComponent FLOAT_TYPE = IChatBaseComponent.literal("f").withStyle(TextComponentTagVisitor.SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
    private static final IChatBaseComponent DOUBLE_TYPE = IChatBaseComponent.literal("d").withStyle(TextComponentTagVisitor.SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
    private static final IChatBaseComponent BYTE_ARRAY_TYPE = IChatBaseComponent.literal("B").withStyle(TextComponentTagVisitor.SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
    private final String indentation;
    private int indentDepth;
    private int depth;
    private final IChatMutableComponent result = IChatBaseComponent.empty();

    public TextComponentTagVisitor(String s) {
        this.indentation = s;
    }

    public IChatBaseComponent visit(NBTBase nbtbase) {
        nbtbase.accept((TagVisitor) this);
        return this.result;
    }

    @Override
    public void visitString(NBTTagString nbttagstring) {
        String s = NBTTagString.quoteAndEscape(nbttagstring.getAsString());
        String s1 = s.substring(0, 1);
        IChatMutableComponent ichatmutablecomponent = IChatBaseComponent.literal(s.substring(1, s.length() - 1)).withStyle(TextComponentTagVisitor.SYNTAX_HIGHLIGHTING_STRING);

        this.result.append(s1).append((IChatBaseComponent) ichatmutablecomponent).append(s1);
    }

    @Override
    public void visitByte(NBTTagByte nbttagbyte) {
        this.result.append((IChatBaseComponent) IChatBaseComponent.literal(String.valueOf(nbttagbyte.getAsNumber())).withStyle(TextComponentTagVisitor.SYNTAX_HIGHLIGHTING_NUMBER)).append(TextComponentTagVisitor.BYTE_TYPE);
    }

    @Override
    public void visitShort(NBTTagShort nbttagshort) {
        this.result.append((IChatBaseComponent) IChatBaseComponent.literal(String.valueOf(nbttagshort.getAsNumber())).withStyle(TextComponentTagVisitor.SYNTAX_HIGHLIGHTING_NUMBER)).append(TextComponentTagVisitor.SHORT_TYPE);
    }

    @Override
    public void visitInt(NBTTagInt nbttagint) {
        this.result.append((IChatBaseComponent) IChatBaseComponent.literal(String.valueOf(nbttagint.getAsNumber())).withStyle(TextComponentTagVisitor.SYNTAX_HIGHLIGHTING_NUMBER));
    }

    @Override
    public void visitLong(NBTTagLong nbttaglong) {
        this.result.append((IChatBaseComponent) IChatBaseComponent.literal(String.valueOf(nbttaglong.getAsNumber())).withStyle(TextComponentTagVisitor.SYNTAX_HIGHLIGHTING_NUMBER)).append(TextComponentTagVisitor.LONG_TYPE);
    }

    @Override
    public void visitFloat(NBTTagFloat nbttagfloat) {
        this.result.append((IChatBaseComponent) IChatBaseComponent.literal(String.valueOf(nbttagfloat.getAsFloat())).withStyle(TextComponentTagVisitor.SYNTAX_HIGHLIGHTING_NUMBER)).append(TextComponentTagVisitor.FLOAT_TYPE);
    }

    @Override
    public void visitDouble(NBTTagDouble nbttagdouble) {
        this.result.append((IChatBaseComponent) IChatBaseComponent.literal(String.valueOf(nbttagdouble.getAsDouble())).withStyle(TextComponentTagVisitor.SYNTAX_HIGHLIGHTING_NUMBER)).append(TextComponentTagVisitor.DOUBLE_TYPE);
    }

    @Override
    public void visitByteArray(NBTTagByteArray nbttagbytearray) {
        this.result.append("[").append(TextComponentTagVisitor.BYTE_ARRAY_TYPE).append(";");
        byte[] abyte = nbttagbytearray.getAsByteArray();

        for (int i = 0; i < abyte.length && i < 128; ++i) {
            IChatMutableComponent ichatmutablecomponent = IChatBaseComponent.literal(String.valueOf(abyte[i])).withStyle(TextComponentTagVisitor.SYNTAX_HIGHLIGHTING_NUMBER);

            this.result.append(" ").append((IChatBaseComponent) ichatmutablecomponent).append(TextComponentTagVisitor.BYTE_ARRAY_TYPE);
            if (i != abyte.length - 1) {
                this.result.append(TextComponentTagVisitor.ELEMENT_SEPARATOR);
            }
        }

        if (abyte.length > 128) {
            this.result.append(TextComponentTagVisitor.FOLDED);
        }

        this.result.append("]");
    }

    @Override
    public void visitIntArray(NBTTagIntArray nbttagintarray) {
        this.result.append("[").append(TextComponentTagVisitor.INT_TYPE).append(";");
        int[] aint = nbttagintarray.getAsIntArray();

        for (int i = 0; i < aint.length && i < 128; ++i) {
            this.result.append(" ").append((IChatBaseComponent) IChatBaseComponent.literal(String.valueOf(aint[i])).withStyle(TextComponentTagVisitor.SYNTAX_HIGHLIGHTING_NUMBER));
            if (i != aint.length - 1) {
                this.result.append(TextComponentTagVisitor.ELEMENT_SEPARATOR);
            }
        }

        if (aint.length > 128) {
            this.result.append(TextComponentTagVisitor.FOLDED);
        }

        this.result.append("]");
    }

    @Override
    public void visitLongArray(NBTTagLongArray nbttaglongarray) {
        this.result.append("[").append(TextComponentTagVisitor.LONG_TYPE).append(";");
        long[] along = nbttaglongarray.getAsLongArray();

        for (int i = 0; i < along.length && i < 128; ++i) {
            IChatMutableComponent ichatmutablecomponent = IChatBaseComponent.literal(String.valueOf(along[i])).withStyle(TextComponentTagVisitor.SYNTAX_HIGHLIGHTING_NUMBER);

            this.result.append(" ").append((IChatBaseComponent) ichatmutablecomponent).append(TextComponentTagVisitor.LONG_TYPE);
            if (i != along.length - 1) {
                this.result.append(TextComponentTagVisitor.ELEMENT_SEPARATOR);
            }
        }

        if (along.length > 128) {
            this.result.append(TextComponentTagVisitor.FOLDED);
        }

        this.result.append("]");
    }

    @Override
    public void visitList(NBTTagList nbttaglist) {
        if (nbttaglist.isEmpty()) {
            this.result.append("[]");
        } else if (this.depth >= 64) {
            this.result.append("[").append(TextComponentTagVisitor.FOLDED).append("]");
        } else if (TextComponentTagVisitor.INLINE_ELEMENT_TYPES.contains(nbttaglist.getElementType()) && nbttaglist.size() <= 8) {
            this.result.append("[");

            for (int i = 0; i < nbttaglist.size(); ++i) {
                if (i != 0) {
                    this.result.append(TextComponentTagVisitor.SPACED_ELEMENT_SEPARATOR);
                }

                this.appendSubTag(nbttaglist.get(i), false);
            }

            this.result.append("]");
        } else {
            this.result.append("[");
            if (!this.indentation.isEmpty()) {
                this.result.append("\n");
            }

            String s = Strings.repeat(this.indentation, this.indentDepth + 1);

            for (int j = 0; j < nbttaglist.size() && j < 128; ++j) {
                this.result.append(s);
                this.appendSubTag(nbttaglist.get(j), true);
                if (j != nbttaglist.size() - 1) {
                    this.result.append(this.indentation.isEmpty() ? TextComponentTagVisitor.SPACED_ELEMENT_SEPARATOR : TextComponentTagVisitor.WRAPPED_ELEMENT_SEPARATOR);
                }
            }

            if (nbttaglist.size() > 128) {
                this.result.append(s).append(TextComponentTagVisitor.FOLDED);
            }

            if (!this.indentation.isEmpty()) {
                this.result.append("\n" + Strings.repeat(this.indentation, this.indentDepth));
            }

            this.result.append("]");
        }
    }

    @Override
    public void visitCompound(NBTTagCompound nbttagcompound) {
        if (nbttagcompound.isEmpty()) {
            this.result.append("{}");
        } else if (this.depth >= 64) {
            this.result.append("{").append(TextComponentTagVisitor.FOLDED).append("}");
        } else {
            this.result.append("{");
            Collection<String> collection = nbttagcompound.getAllKeys();

            if (TextComponentTagVisitor.LOGGER.isDebugEnabled()) {
                List<String> list = Lists.newArrayList(nbttagcompound.getAllKeys());

                Collections.sort(list);
                collection = list;
            }

            if (!this.indentation.isEmpty()) {
                this.result.append("\n");
            }

            String s = Strings.repeat(this.indentation, this.indentDepth + 1);
            Iterator<String> iterator = ((Collection) collection).iterator();

            while (iterator.hasNext()) {
                String s1 = (String) iterator.next();

                this.result.append(s).append(handleEscapePretty(s1)).append(": ");
                this.appendSubTag(nbttagcompound.get(s1), true);
                if (iterator.hasNext()) {
                    this.result.append(this.indentation.isEmpty() ? TextComponentTagVisitor.SPACED_ELEMENT_SEPARATOR : TextComponentTagVisitor.WRAPPED_ELEMENT_SEPARATOR);
                }
            }

            if (!this.indentation.isEmpty()) {
                this.result.append("\n" + Strings.repeat(this.indentation, this.indentDepth));
            }

            this.result.append("}");
        }
    }

    private void appendSubTag(NBTBase nbtbase, boolean flag) {
        if (flag) {
            ++this.indentDepth;
        }

        ++this.depth;

        try {
            nbtbase.accept((TagVisitor) this);
        } finally {
            if (flag) {
                --this.indentDepth;
            }

            --this.depth;
        }

    }

    protected static IChatBaseComponent handleEscapePretty(String s) {
        if (TextComponentTagVisitor.SIMPLE_VALUE.matcher(s).matches()) {
            return IChatBaseComponent.literal(s).withStyle(TextComponentTagVisitor.SYNTAX_HIGHLIGHTING_KEY);
        } else {
            String s1 = NBTTagString.quoteAndEscape(s);
            String s2 = s1.substring(0, 1);
            IChatMutableComponent ichatmutablecomponent = IChatBaseComponent.literal(s1.substring(1, s1.length() - 1)).withStyle(TextComponentTagVisitor.SYNTAX_HIGHLIGHTING_KEY);

            return IChatBaseComponent.literal(s2).append((IChatBaseComponent) ichatmutablecomponent).append(s2);
        }
    }

    @Override
    public void visitEnd(NBTTagEnd nbttagend) {}
}
