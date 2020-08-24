package com.centurylink.biwf.screens.networkstatus

import com.centurylink.biwf.model.assia.ApInfo
import com.centurylink.biwf.model.wifi.NetWorkBand

class ModemUtils {
    companion object {
        fun getGuestNetworkName(apiInfo: ApInfo): String {
            var guestNetworkName = ""
            if (apiInfo.ssidMap.containsKey(NetWorkBand.Band5G_Guest4.name)) {
                guestNetworkName =
                    apiInfo.ssidMap.getValue(NetWorkBand.Band5G_Guest4.name)
            } else if (apiInfo.ssidMap.containsKey(NetWorkBand.Band2G_Guest4.name)) {
                guestNetworkName = apiInfo.ssidMap.getValue(NetWorkBand.Band2G_Guest4.name)
            }
            return guestNetworkName
        }

        fun getGuestNetworkState(apiInfo: ApInfo): Boolean {
            return apiInfo.bssidMap.containsValue(NetWorkBand.Band5G_Guest4.name) || apiInfo.bssidMap.containsValue(
                NetWorkBand.Band2G_Guest4.name
            )
        }

        fun getRegularNetworkState(apiInfo: ApInfo) =
            apiInfo.bssidMap.containsValue(NetWorkBand.Band5G.name) || apiInfo.bssidMap.containsValue(
                NetWorkBand.Band2G.name
            )

        fun getRegularNetworkName(modemInfo: ApInfo): String {
            var wifiNetworkName = ""
            if (modemInfo.ssidMap.containsKey(NetWorkBand.Band5G.name)) {
                wifiNetworkName = modemInfo.ssidMap.getValue(NetWorkBand.Band5G.name)
            } else if (modemInfo.ssidMap.containsKey(NetWorkBand.Band2G.name)) {
                wifiNetworkName = modemInfo.ssidMap.getValue(NetWorkBand.Band2G.name)
            }
            return wifiNetworkName
        }
    }
}