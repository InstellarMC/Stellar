package io.papermc.paper.network;

import com.destroystokyo.paper.network.StatusClient;
import net.minecraft.network.Connection;

class PaperStatusClient extends PaperNetworkClient implements StatusClient {

    PaperStatusClient(Connection networkManager) {
        super(networkManager);
    }

}
