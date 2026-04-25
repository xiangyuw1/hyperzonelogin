/*
 * This file is part of HyperZoneLogin, licensed under the GNU Affero General Public License v3.0 or later.
 *
 * Copyright (C) ksqeib (庆灵) <ksqeib@qq.com>
 * Copyright (C) contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package icu.h2l.login.auth.offline.listener

import com.velocitypowered.api.event.Subscribe
import icu.h2l.api.event.auth.MuaFallbackCoordinator
import icu.h2l.api.event.vServer.VServerAuthStartEvent
import icu.h2l.login.auth.offline.service.OfflineAuthService

class OfflineSessionAuthListener(
    private val authService: OfflineAuthService
) {
    @Subscribe
    fun onAuthStart(event: VServerAuthStartEvent) {
        val playerIp = event.proxyPlayer.remoteAddress.address.hostAddress.substringBefore('%')
        val allowOfflineFallback = MuaFallbackCoordinator.isOfflineFallbackCandidate(
            event.proxyPlayer.username,
            event.hyperZonePlayer.clientOriginalUUID,
            playerIp
        )
        if (event.proxyPlayer.isOnlineMode && !allowOfflineFallback) {
            return
        }
        if (!event.hyperZonePlayer.isInWaitingArea()) {
            return
        }
        if (MuaFallbackCoordinator.shouldDeferOfflineFallback(event.proxyPlayer)) {
            return
        }

        val result = authService.tryAutoLogin(event.proxyPlayer) ?: return
        result.message?.let { event.hyperZonePlayer.sendMessage(it) }
    }
}

