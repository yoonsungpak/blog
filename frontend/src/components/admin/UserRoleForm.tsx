import type { User } from "@/lib/user/domain/Users";

export default function UserRoleForm({ role }: { role: User["role"] }) {
    return (
        <div className="d-flex align-items-center gap-2">
            <select className="form-select form-select-sm w-auto" aria-label="역할" defaultValue={role} disabled>
                <option value="VISITOR">방문자</option>
                <option value="WRITER">작성자</option>
                <option value="ADMIN">관리자</option>
            </select>
            <button className="btn btn-sm btn-outline-dark text-nowrap" type="button" disabled>저장</button>
        </div>
    );
}
