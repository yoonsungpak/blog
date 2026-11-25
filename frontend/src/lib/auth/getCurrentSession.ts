import { cookies } from "next/headers";
import { cache } from "react";
import { createClient, RESP_TYPES } from "redis";

const sessionNamespace = process.env.SPRING_SESSION_DATA_REDIS_NAMESPACE ?? "blog:session";
const redis = createClient({
    url: process.env.REDIS_URL ?? "redis://127.0.0.1:6379",
    socket: { connectTimeout: 1500, reconnectStrategy: false },
    disableOfflineQueue: true,
});
redis.on("error", () => {});
const binaryRedis = redis.withTypeMapping({ [RESP_TYPES.BLOB_STRING]: Buffer });
let connecting: Promise<unknown> | undefined;

const serializedStringHeader = Buffer.from([0xac, 0xed, 0x00, 0x05, 0x74]);

function readSessionString(value: Buffer | null): string | null {
    // Spring Session stores a short Java String as a five-byte header, length and string bytes.
    if (!value || value.length < 7 ||
        !value.subarray(0, 5).equals(serializedStringHeader) ||
        value.readUInt16BE(5) !== value.length - 7) {
        return null;
    }

    return value.toString("utf8", 7);
}

export async function getCurrentSession() {
    return getSessionBySid((await cookies()).get("sid")?.value);
}

export const getSessionBySid = cache(async (sid?: string) => {
    if (!sid) {
        return null;
    }

    const sessionId = Buffer.from(sid, "base64").toString("utf8");

    try {
        if (!redis.isReady) {
            connecting ??= redis.connect().finally(() => {
                connecting = undefined;
            });
            await connecting;
        }

        const [creationTime, userId, username] = await binaryRedis.hmGet(
            `${sessionNamespace}:sessions:${sessionId}`,
            ["creationTime", "sessionAttr:userId", "sessionAttr:username"]
        );
        return creationTime ? {
            userId: readSessionString(userId),
            username: readSessionString(username),
        } : null;
    } catch {
        return null;
    }
});
