import Image from "next/image";
import Link from "next/link";
import styles from "./PostCard.module.css";

type PostCardProps = {
    href: string;
    cover?: { src: string; alt: string };
    coverSizes: string;
    boardName: string;
    title: string;
    excerpt: string;
};

export default function PostCard({ href, cover, coverSizes, boardName, title, excerpt }: PostCardProps) {
    return (
        <article className={`${styles.postRow} position-relative`}>
            {cover && (
                <div className={styles.cover}>
                    <Image src={cover.src} alt={cover.alt} fill sizes={coverSizes} />
                </div>
            )}
            <div className={styles.postBody}>
                <p className={styles.boardLabel}>{boardName}</p>
                <h2><Link className="stretched-link" href={href}>{title}</Link></h2>
                <p className={styles.excerpt}>{excerpt}</p>
            </div>
        </article>
    );
}
