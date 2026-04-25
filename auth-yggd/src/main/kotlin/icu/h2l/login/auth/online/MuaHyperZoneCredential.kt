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

import icu.h2l.api.profile.HyperZoneCredential
import icu.h2l.login.auth.online.db.EntryDatabaseHelper
import java.util.*

/**
 * MUA 认证凭证。
 *
 * MUA（Minecraft Universe Authentication）是基于 Yggdrasil 协议的外部认证服务，
 * 面向携带离线 UUID 的玩家：先尝试 MUA 验证，失败则回退到离线认证模块。
 *
 * 凭证渠道 ID 固定为 [CHANNEL_ID]（"mua"），与普通 Yggdrasil 渠道（"yggdrasil"）严格隔离。
 * 数据库入口 ID 同样为 "mua"，对应由 auth-yggd 模块在 auth-yggd/ 目录下加载的同名 Entry 配置。
 */
class MuaHyperZoneCredential(
    private val entryDatabaseHelper: EntryDatabaseHelper,
    private val authenticatedName: String,
    private val authenticatedUUID: UUID,
    private val suggestedProfileCreateUuid: UUID?,
    private val knownProfileId: UUID? = null
) : HyperZoneCredential {

    override val channelId: String = CHANNEL_ID
    override val credentialId: String = "$CHANNEL_ID:$authenticatedUUID"

    override fun getRegistrationName(): String = authenticatedName

    override fun getBoundProfileId(): UUID? {
        return knownProfileId ?: entryDatabaseHelper.findEntryByUuid(CHANNEL_ID, authenticatedUUID)
    }

    override fun getSuggestedProfileCreateUuid(): UUID? = suggestedProfileCreateUuid

    override fun validateBind(profileId: UUID): String? {
        val currentProfileId = getBoundProfileId()
        if (currentProfileId != null && currentProfileId != profileId) {
            return "MUA 凭证 $credentialId 已绑定到其他 Profile: $currentProfileId"
        }
        return null
    }

    override fun bind(profileId: UUID): Boolean {
        return entryDatabaseHelper.createEntry(
            entryId = CHANNEL_ID,
            name = authenticatedName,
            uuid = authenticatedUUID,
            pid = profileId
        )
    }

    /**
     * 创建一个更新了建议 UUID 的凭证副本，用于 ReUuid 流程。
     *
     * 传入 null 表示由核心 ReUuid 逻辑自行决定新 UUID。
     */
    fun withNewSuggestedUuid(newSuggestedUuid: UUID?): MuaHyperZoneCredential {
        return MuaHyperZoneCredential(
            entryDatabaseHelper = entryDatabaseHelper,
            authenticatedName = authenticatedName,
            authenticatedUUID = authenticatedUUID,
            suggestedProfileCreateUuid = newSuggestedUuid,
            knownProfileId = knownProfileId
        )
    }

    companion object {
        const val CHANNEL_ID = "mua"
    }
}
