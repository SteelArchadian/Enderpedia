package dev.enderpedia;

import dev.enderpedia.client.EnderpediaClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(Enderpedia.MODID)
public final class Enderpedia {
    public static final String MODID = "enderpedia";
    public static final Logger LOGGER = LoggerFactory.getLogger("Enderpedia");

    public Enderpedia(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_SPEC);
        PlayerBooks.register(modBus);
        modBus.addListener(Network::register);
        NeoForge.EVENT_BUS.register(JoinBooks.class);
        NeoForge.EVENT_BUS.register(Commands.class);
        if (FMLEnvironment.dist == Dist.CLIENT) EnderpediaClient.init(modBus, container);
    }
}
