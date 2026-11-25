import Link from "next/link";
import styles from "./ErrorPage.module.css";

type ErrorPageProps = {
    code: 404 | 500;
    title: string;
    description: string;
};

export default function ErrorPage({ code, title, description }: ErrorPageProps) {
    return (
        <section className={styles.error} aria-labelledby="error-title">
            <p className={styles.code}>{code}</p>
            <h1 id="error-title">{title}</h1>
            <p className={styles.description}>{description}</p>
            <Link className={styles.home} href="/">홈으로</Link>
        </section>
    );
}
