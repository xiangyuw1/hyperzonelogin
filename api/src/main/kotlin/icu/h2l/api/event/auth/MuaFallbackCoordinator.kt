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

package icu.h2l.api.event.auth

import com.velocitypowered.api.proxy.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * 协调离线认证与 MUA 优先级的轻量运行时注册表。
 *
 * `auth-yggd` 可注册“当前玩家是否应等待 MUA 先完成”的判断逻辑；
 * `auth-offline` 可注册“MUA 失败后如何继续离线流程”的回调。
 *
 * 之所以放在 API 层，是为了避免两个子模块在编译期产生直接依赖。
 */
object MuaFallbackCoordinator {
    @Volatile
    private var deferOfflineHandler: ((Player) -> Boolean)? = null

    @Volatile
    private var offlineFallbackContinuation: ((Player) -> Unit)? = null

    @Volatile
    private var muaAvailabilityHandler: (() -> Boolean)? = null

    @Volatile
    private var muaSessionAuthHandler: ((String, UUID) -> Boolean)? = null

    private val offlineFallbackCandidates = ConcurrentHashMap.newKeySet<String>()

    fun bindDeferOfflineHandler(handler: (Player) -> Boolean) {
        deferOfflineHandler = handler
    }

    fun shouldDeferOfflineFallback(player: Player): Boolean {
        return deferOfflineHandler?.invoke(player) == true
    }

    fun bindOfflineFallbackContinuation(continuation: (Player) -> Unit) {
        offlineFallbackContinuation = continuation
    }

    fun continueOfflineFallback(player: Player) {
        offlineFallbackContinuation?.invoke(player)
    }

    fun bindMuaAvailabilityHandler(handler: () -> Boolean) {
        muaAvailabilityHandler = handler
    }

    fun hasMuaProvider(): Boolean {
        return muaAvailabilityHandler?.invoke() == true
    }

    fun bindMuaSessionAuthHandler(handler: (String, UUID) -> Boolean) {
        muaSessionAuthHandler = handler
    }

    fun shouldRequestMuaSessionAuth(userName: String, uuid: UUID): Boolean {
        return muaSessionAuthHandler?.invoke(userName, uuid) == true
    }

    fun markOfflineFallbackCandidate(userName: String, uuid: UUID, playerIp: String) {
        offlineFallbackCandidates += candidateKey(userName, uuid, playerIp)
    }

    fun isOfflineFallbackCandidate(userName: String, uuid: UUID, playerIp: String): Boolean {
        return candidateKey(userName, uuid, playerIp) in offlineFallbackCandidates
    }

    fun clearOfflineFallbackCandidate(userName: String, uuid: UUID, playerIp: String) {
        offlineFallbackCandidates -= candidateKey(userName, uuid, playerIp)
    }

    fun isOfflineFallbackCandidate(player: Player): Boolean {
        val playerIp = player.remoteAddress.address.hostAddress.substringBefore('%')
        return isOfflineFallbackCandidate(player.username, player.uniqueId, playerIp)
    }

    private fun candidateKey(userName: String, uuid: UUID, playerIp: String): String {
        return "${userName.lowercase()}|$uuid|$playerIp"
    }
}
