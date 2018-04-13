package net;
import key.KeyUtil;
import org.bouncycastle.crypto.tls.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.util.Arrays;
import keygen.Keygen;

public class TLSHandler {

    public static class NetClipTlsServer extends PSKTlsServer {

        public static TlsPSKIdentityManager getPSKIdentityManager() {
            return new TlsPSKIdentityManager() {

                @Override
                public byte[] getHint() {
                    return "Clip".getBytes();
                }

                @Override
                public byte[] getPSK(byte[] bytes) {
                    assert Arrays.equals(bytes, "Clip".getBytes());
                    return KeyUtil.getKey();
                }
            };
        }

        public NetClipTlsServer() {
            super(getPSKIdentityManager());
        }

        public NetClipTlsServer(TlsCipherFactory tlsCipherFactory) {
            super(tlsCipherFactory, getPSKIdentityManager());
        }
    }

    public static class NetClipTlsClient extends PSKTlsClient {

        public static TlsPSKIdentity getPSKIdentity() {

            return new BasicTlsPSKIdentity("Clip", KeyUtil.getKey());
        }

        public NetClipTlsClient() {
            super(getPSKIdentity());
        }

        public NetClipTlsClient(TlsCipherFactory tlsCipherFactory) {
            super(tlsCipherFactory, getPSKIdentity());
        }

        @Override
        public int[] getCipherSuites() {
            return new int[] {
                    CipherSuite.TLS_DHE_PSK_WITH_AES_128_CBC_SHA256,
                    CipherSuite.TLS_DHE_PSK_WITH_AES_128_GCM_SHA256,
                    CipherSuite.TLS_DHE_PSK_WITH_AES_128_CCM,
                    CipherSuite.TLS_ECDHE_PSK_WITH_AES_128_CBC_SHA256
            };
        }
    }

    public static TlsClientProtocol getClientProtocol(InputStream inputStream, OutputStream outputStream) {
        PSKTlsClient client = new NetClipTlsClient();
        TlsClientProtocol protocol = new TlsClientProtocol(inputStream, outputStream, new SecureRandom());
        try {
            protocol.connect(client);
        } catch (TlsFatalAlert|TlsFatalAlertReceived alert) {
            if (alert.getMessage().contains("20")){
                System.out.println("Authentication fails for TLS Connection.");
            } else alert.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return protocol;
    }

    public static TlsServerProtocol getServerProtocol(InputStream inputStream, OutputStream outputStream) {
        PSKTlsServer server = new NetClipTlsServer();
        TlsServerProtocol protocol = new TlsServerProtocol(inputStream, outputStream, new SecureRandom());
        try {
            protocol.accept(server);
        } catch (TlsFatalAlert|TlsFatalAlertReceived alert) {
            if (alert.getMessage().contains("20")){
                System.out.println("Authentication fails for TLS Connection.");
            } else alert.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return protocol;
    }

    public static TlsProtocol getTlsProtocol(boolean isServer, InputStream inputStream, OutputStream outputStream) {
        return isServer ?
                getServerProtocol(inputStream, outputStream) :
                getClientProtocol(inputStream, outputStream);
    }
}
