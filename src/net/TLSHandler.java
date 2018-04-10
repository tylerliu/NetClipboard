package net;
import org.bouncycastle.crypto.tls.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.util.Arrays;
import Keygen.Keygen;

public class TLSHandler {

    public static class NetClipTlsServer extends PSKTlsServer {

        public static TlsPSKIdentityManager getPSKIdentityManager(File keyFile) {
            return new TlsPSKIdentityManager() {

                @Override
                public byte[] getHint() {
                    return "Clip".getBytes();
                }

                @Override
                public byte[] getPSK(byte[] bytes) {
                    assert Arrays.equals(bytes, "Clip".getBytes());
                    return Keygen.fileToKey(keyFile);
                }
            };
        }

        public NetClipTlsServer(File keyFile) {
            super(getPSKIdentityManager(keyFile));
        }

        public NetClipTlsServer(TlsCipherFactory tlsCipherFactory, File keyFile) {
            super(tlsCipherFactory, getPSKIdentityManager(keyFile));
        }
    }

    public static class NetClipTlsClient extends PSKTlsClient {

        public static TlsPSKIdentity getPSKIdentity(File keyFile) {

            return new BasicTlsPSKIdentity("Clip", Keygen.fileToKey(keyFile));
        }

        public NetClipTlsClient(File keyFile) {
            super(getPSKIdentity(keyFile));
        }

        public NetClipTlsClient(TlsCipherFactory tlsCipherFactory, File keyFile) {
            super(tlsCipherFactory, getPSKIdentity(keyFile));
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

    public static TlsClientProtocol getClientProtocol(InputStream inputStream, OutputStream outputStream, File keyFile) {
        PSKTlsClient client = new NetClipTlsClient(keyFile);
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

    public static TlsServerProtocol getServerProtocol(InputStream inputStream, OutputStream outputStream, File keyFile) {
        PSKTlsServer server = new NetClipTlsServer(keyFile);
        TlsServerProtocol protocol = new TlsServerProtocol(inputStream, outputStream, new SecureRandom());
        try {
            protocol.accept(server);
        } catch (TlsFatalAlert|TlsFatalAlertReceived alert) {
            alert.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return protocol;
    }

    public static TlsProtocol getTlsProtocol(boolean isServer, InputStream inputStream, OutputStream outputStream, File keyFile) {
        return isServer ?
                getServerProtocol(inputStream, outputStream, keyFile) :
                getClientProtocol(inputStream, outputStream, keyFile);
    }
}
