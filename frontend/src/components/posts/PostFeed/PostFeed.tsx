import Link from "next/link";
import Profile from "@/components/profile/Profile";
import { samplePosts } from "@/data/SamplePosts";
import type { Board } from "@/lib/board/domain/Boards";
import BoardRepository from "@/lib/board/repository/BoardRepository";
import PostCard from "../PostCard/PostCard";
import styles from "./PostFeed.module.css";

export default async function PostFeed({ board }: { board?: Board }) {
    const repository = new BoardRepository();
    const [boards, firstBoard] = await Promise.all([repository.findVisible(), repository.findFirstVisible()]);
    const visiblePosts = firstBoard?.url && (!board || board.id === firstBoard.id)
        ? samplePosts
        : [];

    return (
        <div className={styles.feed}>
            {!board && <Profile />}

            <nav className={`${styles.boardNav} d-flex flex-wrap`} aria-label="게시판">
                <Link href="/" aria-current={board ? undefined : "page"}>전체 글</Link>
                {boards.map(({ id, url, name }) => url && (
                    <Link key={id} href={`/${url}`} aria-current={board?.id === id ? "page" : undefined}>
                        {name ?? url}
                    </Link>
                ))}
            </nav>

            <section aria-labelledby="feed-heading">
                <h1 id="feed-heading" className={styles.feedTitle}>{board?.name ?? "전체 글"}</h1>
                <div className={board ? styles.boardGrid : styles.postList}>
                    {visiblePosts.map(({ slug, cover, title, excerpt }) => (
                        <PostCard
                            key={slug}
                            href={`/posts/${slug}`}
                            boardName={firstBoard?.name ?? firstBoard?.url ?? ""}
                            cover={cover}
                            coverSizes={board ? "(min-width: 992px) 380px, 100vw" : "(min-width: 1232px) 1184px, 100vw"}
                            title={title}
                            excerpt={excerpt}
                        />
                    ))}
                </div>
            </section>
        </div>
    );
}
