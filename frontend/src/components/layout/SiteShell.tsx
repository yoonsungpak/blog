import { Suspense, type ReactNode } from "react";
import Navbar from "./Navbar";
import styles from "./SiteShell.module.css";

export default function SiteShell({ children }: { children: ReactNode }) {
    return (
        <div className={styles.shell}>
            <header className={`${styles.header} ${styles.container}`}>
                <Suspense fallback={null}>
                    <Navbar />
                </Suspense>
            </header>
            <main className={`${styles.main} ${styles.container}`}>{children}</main>
            <footer className={styles.footer}>
                <div className={styles.container}>
                    <p className="mb-0">© 2026 blog. All rights reserved.</p>
                </div>
            </footer>
        </div>
    );
}
