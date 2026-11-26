import Link from "next/link";
import styles from "./BoardEditor.module.css";

export default function BoardEditor({ board }: { board?: { name: string; slug: string } }) {
    const creating = !board;
    return (
        <div className={styles.backdrop}>
            <section className={styles.editor} role="dialog" aria-modal="true" aria-labelledby="board-editor-title">
                <header className="d-flex align-items-center justify-content-between gap-3 mb-4">
                    <h2 className="h5 mb-0" id="board-editor-title">
                        {creating ? "게시판 만들기" : "게시판 수정"}
                    </h2>
                    <Link className="btn-close" href="/admin/boards" scroll={false} aria-label="닫기" />
                </header>
                <div className="d-grid gap-3">
                    <div>
                        <label className="form-label" htmlFor="board-name">이름</label>
                        <input className="form-control rounded-3" id="board-name" name="name"
                            defaultValue={board?.name ?? ""} />
                    </div>
                    <div>
                        <label className="form-label" htmlFor="board-slug">주소</label>
                        <div className="input-group">
                            <span className="input-group-text">/</span>
                            <input className="form-control" id="board-slug" name="slug"
                                defaultValue={board?.slug ?? ""} />
                        </div>
                    </div>
                    <div className="form-check form-switch">
                        <input className="form-check-input" id="board-public" name="public"
                            type="checkbox" role="switch" defaultChecked />
                        <label className="form-check-label" htmlFor="board-public">공개</label>
                    </div>
                </div>
                <p className="small text-body-secondary mt-4 mb-3">게시판 변경을 저장하는 기능은 아직 연결되지 않았습니다.</p>
                <footer className={styles.actions}>
                    {!creating && (
                        <button className="btn btn-outline-danger rounded-3 me-auto" type="button" disabled>
                            삭제
                            <span className={`${styles.icon} ${styles.trash}`} aria-hidden="true" />
                        </button>
                    )}
                    <Link className="btn btn-outline-secondary rounded-3" href="/admin/boards" scroll={false}>취소</Link>
                    <button className={`btn rounded-3 ${styles.primary}`} type="button" disabled>
                        저장
                        <span className={`${styles.icon} ${styles.floppy}`} aria-hidden="true" />
                    </button>
                </footer>
            </section>
        </div>
    );
}
