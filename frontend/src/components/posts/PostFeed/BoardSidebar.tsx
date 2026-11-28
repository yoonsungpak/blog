"use client";

import { useId, useState, type ReactNode } from "react";
import styles from "./PostFeed.module.css";

export default function BoardSidebar({ children }: { children: ReactNode }) {
    const menuId = useId();
    const [expanded, setExpanded] = useState(false);

    return (
        <div className={styles.boardSidebar}>
            <button
                className={styles.boardToggle}
                type="button"
                aria-controls={menuId}
                aria-expanded={expanded}
                onClick={() => setExpanded(!expanded)}
            >
                <span className={styles.menuIcon} aria-hidden="true" />
                게시판
            </button>
            <nav className={styles.boardMenu} id={menuId} data-expanded={expanded} aria-label="게시판">
                {children}
            </nav>
        </div>
    );
}
