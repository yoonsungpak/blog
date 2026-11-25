const referenceOrigin = "https://internal.invalid";

export function getSafeRedirect(search: string): string | null {
    const target = new URLSearchParams(search).get("redirect");
    if (!target?.startsWith("/") || target.startsWith("//")) {
        return null;
    }

    try {
        const { origin, pathname, search: query, hash } = new URL(target, referenceOrigin);
        if (origin !== referenceOrigin || pathname.startsWith("//")) {
            return null;
        }

        return pathname + query + hash;
    } catch {
        return null;
    }
}
