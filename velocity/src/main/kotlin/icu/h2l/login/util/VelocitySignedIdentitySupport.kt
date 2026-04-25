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

package icu.h2l.login.util

import com.velocitypowered.api.proxy.crypto.IdentifiedKey
import java.util.UUID

/**
 * 判断客户端的签名身份是否仍能安全转发到当前目标 UUID。
 *
 * MUA / reUUID 场景里，Velocity 当前持有的玩家最终 UUID 可能已经不是
 * 客户端最初进入代理时的 holder UUID。此时若继续把旧的 identified key
 * 转发到后端，1.19+ 后端容易以 `invalid_public_key_signature` 直接踢人。
 */
internal fun shouldForwardIdentifiedKey(identifiedKey: IdentifiedKey?, targetUuid: UUID): Boolean {
    val key = identifiedKey ?: return false
    val signatureHolder = key.signatureHolder ?: return true
    return signatureHolder == targetUuid
}
