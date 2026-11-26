import type { Metadata } from "next";
import { notFound } from "next/navigation";
import { Suspense } from "react";
import PostFeed from "@/components/posts/PostFeed/PostFeed";
import BoardRepository from "@/lib/board/repository/BoardRepository";

export async function generateMetadata({ params }: PageProps<"/[board]">): Promise<Metadata> {
    const [{ board }, boards] = await Promise.all([params, new BoardRepository().findAll()]);
    return boards.some(({ url, isPublic }) => isPublic && url === board) ? {} : { title: "404 | Blog" };
}

export default function BoardPage({ params }: PageProps<"/[board]">) {
    return (
        <Suspense fallback={null}>
            <BoardFeed params={params} />
        </Suspense>
    );
}

async function BoardFeed({ params }: Pick<PageProps<"/[board]">, "params">) {
    const [{ board: slug }, boards] = await Promise.all([params, new BoardRepository().findAll()]);
    const board = boards.find(({ url, isPublic }) => isPublic && url === slug);

    if (!board) {
        notFound();
    }

    return <PostFeed board={board} />;
}
