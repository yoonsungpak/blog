"use client";

import { useState } from "react";

export default function LogoutButton({ className }: { className: string }) {
    const [pending, setPending] = useState(false);

    async function logout() {
        setPending(true);
        try {
            const response = await fetch("/api/auth/logout", {
                method: "POST",
                redirect: "error",
            });
            if (!response.ok) {
                throw new Error("Logout request failed");
            }
            window.location.reload();
        } catch {
            alert("로그아웃을 완료하지 못했습니다.");
            setPending(false);
        }
    }

    return <button className={className} type="button" onClick={logout} disabled={pending}>로그아웃</button>;
}
