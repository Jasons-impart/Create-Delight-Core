package io.github.jasonsimpart.createdelightcore.data;

import com.tterrag.registrate.providers.ProviderType;
import io.github.jasonsimpart.createdelightcore.data.lang.ChineseLangHandler;
import io.github.jasonsimpart.createdelightcore.data.lang.EnglishLangHandler;
import io.github.jasonsimpart.createdelightcore.data.lang.RegistrateCNLangProvider;

import static io.github.jasonsimpart.createdelightcore.registry.CDRegistration.REGISTRATE;

public class CDCoreDatagen {
    public static final ProviderType<RegistrateCNLangProvider> CNLANG = ProviderType.register("cdcore_cn_lang", (p, e) -> new RegistrateCNLangProvider(p, e.getGenerator().getPackOutput()));

    public static void init() {
        REGISTRATE.addDataGenerator(ProviderType.LANG, EnglishLangHandler::init);
        REGISTRATE.addDataGenerator(CNLANG, ChineseLangHandler::init);
    }
}
