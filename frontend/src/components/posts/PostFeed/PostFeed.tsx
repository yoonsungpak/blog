import Link from "next/link";
import Profile from "@/components/profile/Profile";
import { boards } from "@/data/boards";
import { samplePosts } from "@/data/SamplePosts";
import PostCard from "../PostCard/PostCard";
import styles from "./PostFeed.module.css";

export default function PostFeed({ board }: { board?: (typeof boards)[number] }) {
    const visiblePosts = board ? samplePosts.filter((post) => post.board.slug === board.slug) : samplePosts;

    return (
        <div className={styles.feed}>
            {!board && <Profile />}

            <nav className={`${styles.boardNav} d-flex flex-wrap`} aria-label="게시판">
                <Link href="/" aria-current={board ? undefined : "page"}>전체 글</Link>
                {boards.map(({ slug, name }) => (
                    <Link key={slug} href={`/${slug}`} aria-current={board?.slug === slug ? "page" : undefined}>
                        {name}
                    </Link>
                ))}
            </nav>

            <section aria-labelledby="feed-heading">
                <h1 id="feed-heading" className={styles.feedTitle}>{board?.name ?? "전체 글"}</h1>
                <div className={board ? styles.boardGrid : styles.postList}>
                    {visiblePosts.map(({ board: postBoard, slug, cover, title, excerpt }) => (
                        <PostCard
                            key={slug}
                            href={`/posts/${slug}`}
                            boardName={postBoard.name}
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
