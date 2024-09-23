//Contains code from: https://github.com/OKTW-Network/FabricProxy-Lite/blob/master/src/main/java/one/oktw/VelocityLib.java
package org.adde0109.pcf;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import io.netty.buffer.Unpooled;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.login.ServerboundCustomQueryAnswerPacket;
import org.adde0109.pcf.login.IMixinConnection;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.Nullable;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ModernForwarding {

    private static final int SUPPORTED_FORWARDING_VERSION = 1;

    private final String forwardingSecret;

    ModernForwarding(String forwardingSecret) {
        this.forwardingSecret = forwardingSecret;
    }


    @Nullable
    public GameProfile handleForwardingPacket(ServerboundCustomQueryAnswerPacket packet, Connection connection) throws Exception {
//        FriendlyByteBuf data = packet.getInternalData();
//        if(data == null) {
//            throw new Exception("Got empty packet");
//        }
        if(packet.payload() == null) {
            throw new Exception("Got empty packet");
        }

        FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());
        packet.payload().write(data);

        if(data.readableBytes() == 0) {
            throw new Exception("Got empty packet (No readable bytes)");
        }

        //
        System.out.println("boop1");
        //

        // Not entirely sure what byte we're skipping here, but without this skip, the rest of this function will
        // not work properly.
        data.skipBytes(1);

        //
        System.out.println("boop2");
        //

        if(!validate(data)) {
            throw new Exception("Player-data could not be validated!");
        }
        LogManager.getLogger().debug("Player-data validated!");

        //
        System.out.println("boop3");
        //

        int version = data.readVarInt();
        if (version != SUPPORTED_FORWARDING_VERSION) {
            throw new IllegalStateException("Unsupported forwarding version " + version + ", wanted " + SUPPORTED_FORWARDING_VERSION);
        }

        String ip = data.readUtf(Short.MAX_VALUE);
        SocketAddress address = connection.getRemoteAddress();
        int port = 0;
        if (address instanceof InetSocketAddress) {
            port = ((InetSocketAddress) address).getPort();
        }

        ((IMixinConnection) connection).pcf$setAddress(new InetSocketAddress(ip, port));

        GameProfile profile = new GameProfile(data.readUUID(), data.readUtf(16));
        readProperties(data, profile);
        return profile;
    }

    public boolean validate(FriendlyByteBuf buffer) {
        //
        System.out.println("boop2.1");
        //

        final byte[] signature = new byte[32];
        buffer.readBytes(signature);

        //
        System.out.println("boop2.2");
        //

        final byte[] data = new byte[buffer.readableBytes()];
        buffer.getBytes(buffer.readerIndex(), data);

        //
        System.out.println("boop2.3");
        //

        try {
            final Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(forwardingSecret.getBytes(), "HmacSHA256"));
            final byte[] mySignature = mac.doFinal(data);
            //
            System.out.println("boop2.4");
            //
            if (!MessageDigest.isEqual(signature, mySignature)) {
                return false;
            }
        } catch (final InvalidKeyException | NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }

        return true;
    }

    public void readProperties(FriendlyByteBuf buf, GameProfile profile) {
        PropertyMap properties = profile.getProperties();
        int size = buf.readVarInt();
        for (int i = 0; i < size; i++) {
            String name = buf.readUtf();
            String value = buf.readUtf();
            String signature = "";
            boolean hasSignature = buf.readBoolean();
            if (hasSignature) {
                signature = buf.readUtf();
            }
            properties.put(name, new Property(name, value, signature));
        }
    }
}
