import styles from "./Pagination.module.css";

export default function Pagination({ page, totalPages, onPageChange }) {
    return (
        <>
            {totalPages > 0 && (
                <div className={styles.pagination}>
                    <button
                        onClick={() => onPageChange(page - 1)}
                        disabled={page === 0}
                    >
                        ◀ Trước
                    </button>
                    <span>Trang {page + 1} / {totalPages}</span>
                    <button
                        onClick={() => onPageChange(page + 1)}
                        disabled={page + 1 >= totalPages}
                    >
                        Sau ▶
                    </button>
                </div>
            )}
        </>
    );
}