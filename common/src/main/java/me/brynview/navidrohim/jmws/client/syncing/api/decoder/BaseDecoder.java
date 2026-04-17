package me.brynview.navidrohim.jmws.client.syncing.api.decoder;

import me.brynview.navidrohim.jmws.client.syncing.api.ClientObjectWrapper;

public interface BaseDecoder<W extends ClientObjectWrapper<?>, N extends Object>
{
    W decodeStringToWrapper(String data);
    N decodeStringToNative(String data);
}
