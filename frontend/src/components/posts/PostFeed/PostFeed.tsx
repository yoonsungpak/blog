import Link from "next/link";
import Profile from "@/components/profile/Profile";
import { samplePosts } from "@/data/SamplePosts";
import type { Board } from "@/lib/board/domain/Boards";
import { groupBoards } from "@/lib/board/groupBoards";
import BoardRepository from "@/lib/board/repository/BoardRepository";
import PostCard from "../PostCard/PostCard";
import BoardSidebar from "./BoardSidebar";
import styles from "./PostFeed.module.css";

export default async function PostFeed({ board }: { board?: Board }) {
    const repository = new BoardRepository();
    const [boards, firstBoard, categories] = await Promise.all([
        repository.findVisible(), repository.findFirstVisible(), repository.findCategories(),
    ]);
    const visiblePosts = firstBoard?.url && (!board || board.id === firstBoard.id)
        ? samplePosts
        : [];
    const boardLinks = (
        <>
            <Link href="/" aria-current={board ? undefined : "page"}>전체 글</Link>
            {groupBoards(boards.filter(({ url }) => url), categories).map(({ category, boards }) => {
                if (!boards.length) return null;

                const links = boards.map(({ id, url, name }) => (
                    <Link key={id} href={`/${url}`} aria-current={board?.id === id ? "page" : undefined}>
                        {name ?? url}
                    </Link>
                ));
                return category ? (
                    <details key={category.id} className={styles.boardCategory} open={category.id === board?.categoryId}>
                        <summary>{category.name}</summary>
                        <div className={styles.categoryLinks}>{links}</div>
                    </details>
                ) : links;
            })}
        </>
    );
    const posts = (
        <section aria-labelledby="feed-heading">
            <h1 id="feed-heading" className={styles.feedTitle}>{board?.name ?? "전체 글"}</h1>
            <div className={board ? styles.boardGrid : styles.postList}>
                {visiblePosts.map(({ slug, cover, title, excerpt }) => (
                    <PostCard
                        key={slug}
                        href={`/posts/${slug}`}
                        boardName={firstBoard?.name ?? firstBoard?.url ?? ""}
                        cover={cover}
                        coverSizes={board
                            ? "(min-width: 1232px) 294px, (min-width: 992px) calc((100vw - 22rem) / 3), calc(100vw - 3rem)"
                            : "(min-width: 1232px) 928px, (min-width: 992px) calc(100vw - 19rem), calc(100vw - 3rem)"}
                        title={title}
                        excerpt={excerpt}
                    />
                ))}
            </div>
        </section>
    );

    return (
        <div className={styles.feed}>
            {!board && <Profile />}
            <div className={styles.feedLayout}>
                <BoardSidebar>{boardLinks}</BoardSidebar>
                {posts}
            </div>
        </div>
    );
}
