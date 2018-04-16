package io.github.rxcats.core.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ServerAddrUtil {
    private static final List<String> ignoreIfaceNames = Arrays.asList("VMware", "VirtualBox"); // VMware, VirtualBox 네트워크 어댑터는 제외

    private static String serverIp;

    private static String findIp(boolean isSiteLocalAddress) {
        try {
            final Enumeration<NetworkInterface> nienum = NetworkInterface.getNetworkInterfaces();
            while (nienum.hasMoreElements()) {
                final NetworkInterface ni = nienum.nextElement();
                if (ni.getDisplayName() != null &&
                    ignoreIfaceNames
                        .stream()
                        .map(String::toLowerCase)
                        .anyMatch(s -> ni.getDisplayName().toLowerCase().contains(s))) {
                    continue;
                }

                final Enumeration<InetAddress> ia = ni.getInetAddresses();
                while (ia.hasMoreElements()) {
                    final InetAddress inetAddress = ia.nextElement();
                    if (inetAddress instanceof Inet6Address) {
                        continue;
                    }

                    if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress()) {
                        if (isSiteLocalAddress) {
                            if (inetAddress.isSiteLocalAddress()) {
                                return inetAddress.getHostAddress();
                            }
                        } else {
                            // 무선랜 DHCP 의 경우 isSiteLocalAddress 가 false
                            if (!inetAddress.isSiteLocalAddress()) {
                                return inetAddress.getHostAddress();
                            }
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String getServerIp() {
        if (serverIp == null) {
            serverIp = findIp(true);
            if (serverIp == null) {
                serverIp = findIp(false);
            }
        }
        return serverIp;
    }
}
