package com.wavky.cdtimer.common.util

import java.net.InetAddress
import java.net.NetworkInterface
import java.util.Collections

object NetworkUtils {
  fun getLocalIpAddress(): String? {
    try {
      val interfaces = NetworkInterface.getNetworkInterfaces()
      for (networkInterface in Collections.list(interfaces)) {
        val addresses = networkInterface.inetAddresses
        for (address in Collections.list(addresses)) {
          if (!address.isLoopbackAddress && address is InetAddress) {
            val ipAddress = address.hostAddress ?: continue
            if (ipAddress.indexOf(':') < 0) {  // IPv4 only
              return ipAddress
            }
          }
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
    return null
  }
}
