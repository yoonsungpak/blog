import type { Metadata } from "next";
import { cookies } from "next/headers";
import { notFound } from "next/navigation";
import { cache, Suspense } from "react";
import Link from "next/link";
import styles from "@/components/admin/BoardEditor.module.css";
import CategoryModal from "@/components/admin/CategoryModal";
import OrderModal from "@/components/admin/OrderModal";
import CreateBoardModal from "@/components/admin/CreateBoardModal";
import EditBoardModal from "@/components/admin/EditBoardModal";
import BoardRepository from "@/lib/board/repository/BoardRepository";
import { groupBoards } from "@/lib/board/groupBoards";
import UserService from "@/lib/user/service/UserService";

const hasAdminAccess = cache(async () => {
    const sid = (await cookies()).get("sid")?.value;
    return sid ? new UserService().isAdmin(sid) : false;
});

export async function generateMetadata(): Promise<Metadata> {
    return { title: await hasAdminAccess() ? "게시판 관리 | Blog" : "404 | Blog" };
}

async function AdminBoards({ searchParams }: Pick<PageProps<"/admin/boards">, "searchParams">) {
    if (!(await hasAdminAccess())) {
        notFound();
    }

    const repository = new BoardRepository();
    const [{ edit, create, order, category }, boards, categories] = await Promise.all([
        searchParams, repository.findAll(), repository.findCategories(),
    ]);
    const selectedBoard = boards.find((board) => String(board.id) === edit);
    const selectedCategory = categories.find(({ id }) => String(id) === category);
    const groups = groupBoards(boards, categories);
    const orderGroup = groups.find(({ category }) => (category ? String(category.id) : "uncategorized") === order);

    return (
        <section className="py-5" aria-labelledby="boards-title">
            <header className="d-flex flex-wrap align-items-center justify-content-between gap-3 mb-4">
                <h1 id="boards-title" className="h3 mb-0">게시판 관리</h1>
                <div className="d-flex flex-wrap gap-2">
                    <Link className="btn btn-sm btn-outline-secondary rounded-3" href="/admin">
                        관리자 메뉴
                    </Link>
                    <Link className="btn btn-sm btn-outline-secondary rounded-3" href="/admin/boards?category=new" scroll={false}>
                        카테고리 만들기
                    </Link>
                    {categories.length > 1 && (
                        <Link className="btn btn-sm btn-outline-secondary rounded-3" href="/admin/boards?order=categories" scroll={false}>
                            카테고리 순서 수정
                        </Link>
                    )}
                    <Link className={`btn btn-sm rounded-3 ${styles.primary}`} href="/admin/boards?create=1" scroll={false}>
                        새로 만들기
                    </Link>
                </div>
            </header>
            <div className="table-responsive">
                <table className="table align-middle mb-0">
                    <thead>
                        <tr>
                            <th scope="col">게시판 이름</th>
                            <th scope="col">주소</th>
                            <th scope="col">공개 여부</th>
                            <th className="text-end" scope="col">관리</th>
                        </tr>
                    </thead>
                    {groups.map(({ category, boards }) => {
                        const groupName = category?.name ?? "미분류";
                        return (
                            <tbody key={category?.id ?? "uncategorized"}>
                                <tr className="table-light">
                                    <th colSpan={4} scope="rowgroup">
                                        <div className="d-flex flex-wrap align-items-center justify-content-between gap-2">
                                            {groupName}
                                            <div className="d-flex flex-wrap gap-2">
                                                {boards.length > 1 && (
                                                    <Link className="btn btn-sm btn-outline-secondary rounded-3"
                                                        href={{ pathname: "/admin/boards", query: { order: category?.id ?? "uncategorized" } }}
                                                        scroll={false} aria-label={`${groupName} 게시판 순서 수정`}>
                                                        순서 수정
                                                    </Link>
                                                )}
                                                {category && (
                                                    <Link className="btn btn-sm btn-outline-secondary rounded-3"
                                                        href={{ pathname: "/admin/boards", query: { category: category.id } }}
                                                        scroll={false} aria-label={`${groupName} 카테고리 수정`}>
                                                        카테고리 수정
                                                    </Link>
                                                )}
                                            </div>
                                        </div>
                                    </th>
                                </tr>
                                {boards.map(({ id, url, name, isPublic }) => (
                                    <tr key={id}>
                                        <th scope="row">{name ?? "—"}</th>
                                        <td>
                                            {url ? (
                                                <Link className="text-decoration-underline font-monospace" href={`/${url}`}>
                                                    /{url}
                                                </Link>
                                            ) : "—"}
                                        </td>
                                        <td className="text-nowrap">{isPublic ? "공개" : "비공개"}</td>
                                        <td className="text-end">
                                            <Link className="btn btn-sm btn-outline-secondary rounded-3"
                                                href={{ pathname: "/admin/boards", query: { edit: id } }}
                                                scroll={false} aria-label={`${name} 수정`}>
                                                수정
                                            </Link>
                                        </td>
                                    </tr>
                                ))}
                                {!boards.length && (
                                    <tr><td colSpan={4}>등록된 게시판이 없어요.</td></tr>
                                )}
                            </tbody>
                        );
                    })}
                </table>
            </div>
            {create === "1" ? <CreateBoardModal categories={categories} /> : category === "new" || selectedCategory ? (
                <CategoryModal key={selectedCategory?.id ?? "new"} category={selectedCategory} />
            ) : order === "categories" && categories.length > 1 ? (
                <OrderModal key={`categories:${categories.map(({ id }) => id).join(",")}`}
                    items={categories.map(({ id, name }) => ({ id, label: name }))} scope={{ kind: "categories" }} />
            ) : orderGroup && orderGroup.boards.length > 1 ? (
                <OrderModal key={`boards:${orderGroup.boards.map(({ id }) => id).join(",")}`}
                    items={orderGroup.boards.map(({ id, name, url }) => ({ id, label: name ?? url ?? `게시판 ${id}` }))}
                    scope={{ kind: "boards", categoryId: orderGroup.category?.id ?? null }} />
            ) : selectedBoard && (
                <EditBoardModal key={selectedBoard.id} board={selectedBoard} categories={categories} />
            )}
        </section>
    );
}

export default function AdminBoardsPage({ searchParams }: PageProps<"/admin/boards">) {
    return (
        <Suspense fallback={null}>
            <AdminBoards searchParams={searchParams} />
        </Suspense>
    );
}
