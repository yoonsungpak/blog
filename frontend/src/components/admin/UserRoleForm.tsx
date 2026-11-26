"use client";

import type { User } from "@/lib/user/domain/Users";
import React, {type SubmitEvent} from "react";

export default function UserRoleForm({ user }: { user: User}) {
    async function handleSubmit(e: SubmitEvent<HTMLFormElement>) {
        e.preventDefault();

        const formData = new FormData(e.currentTarget);
        try {
            const res = await fetch(`/api/users/${user.id}`, {
                method: "PATCH",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({
                    role: formData.get("role"),
                })
            });

            if (!res.ok) {
                const data = await res.json();
                return alert(data.message);
            }
        } catch {
            return alert("인터넷 연결 상태를 확인해주세요.");
        }

        alert("저장되었습니다.");
        return location.reload();
    }
    return (
        <form data-user-id={user.id} onSubmit={handleSubmit}>
            <div className="d-flex align-items-center gap-2">
                <select className="form-select form-select-sm w-auto" aria-label="역할" name="role" defaultValue={user.role}>
                    <option value="VISITOR">방문자</option>
                    <option value="WRITER">작성자</option>
                    <option value="ADMIN">관리자</option>
                </select>
                <button className="btn btn-sm btn-outline-dark text-nowrap" type="submit">저장</button>
            </div>
        </form>
    );
}
