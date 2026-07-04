package io.github.jasonsimpart.createdelightcore.content.order;

import net.minecraft.network.FriendlyByteBuf;

public record OrderParserLine(String labelKey, String value, String valueTranslationKey,
                              String suffix, String suffixTranslationKey,
                              int color, boolean heading) {
    public OrderParserLine(String labelKey, String value, String valueTranslationKey,
                           String suffix, int color, boolean heading) {
        this(labelKey, value, valueTranslationKey, suffix, "", color, heading);
    }

    public OrderParserLine {
        labelKey = labelKey == null ? "" : labelKey;
        value = value == null ? "" : value;
        valueTranslationKey = valueTranslationKey == null ? "" : valueTranslationKey;
        suffix = suffix == null ? "" : suffix;
        suffixTranslationKey = suffixTranslationKey == null ? "" : suffixTranslationKey;
    }

    public void send(FriendlyByteBuf buf) {
        buf.writeUtf(labelKey, 128);
        buf.writeUtf(value, 128);
        buf.writeUtf(valueTranslationKey, 192);
        buf.writeUtf(suffix, 128);
        buf.writeUtf(suffixTranslationKey, 192);
        buf.writeInt(color);
        buf.writeBoolean(heading);
    }

    public static OrderParserLine receive(FriendlyByteBuf buf) {
        return new OrderParserLine(
                buf.readUtf(128),
                buf.readUtf(128),
                buf.readUtf(192),
                buf.readUtf(128),
                buf.readUtf(192),
                buf.readInt(),
                buf.readBoolean()
        );
    }
}
