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

package icu.h2l.login.auth.online

import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import icu.h2l.api.event.auth.LoginReUuidEvent

/**
 * 响应 ReUuid 事件，销毁旧 MUA 凭证并以清除 UUID 建议的新凭证重新提交。
 *
 * MUA 认证名与 UUID 由 MUA 服务器颁发，不受 rename 影响；
 * reUuid 仅清除凭证中的 suggestedProfileCreateUuid，使核心逻辑选取不冲突的 Profile UUID。
 * 该监听器以 [PostOrder.EARLY] 运行，确保在核心 `LoginReUuidListener` 之前完成凭证替换。
 */
class MuaReUuidListener {

    @Subscribe(order = PostOrder.EARLY)
    fun onReUuid(event: LoginReUuidEvent) {
        val player = event.hyperZonePlayer
        val oldCredential = player.getSubmittedCredentials()
            .filterIsInstance<MuaHyperZoneCredential>()
            .firstOrNull() ?: return

        player.destroyCredential(oldCredential.channelId)
        player.submitCredential(oldCredential.withNewSuggestedUuid(null))
    }
}
