import React, { useEffect, useRef } from 'react';
import styles from './InfoBoard.module.css';

export default function InfoBoard({ product, onClose }) {
    const boardRef = useRef(null);
    const p = product || { productName: '-', price: '-', category: '-', quantity: '-', description: '-' };

    // 👇 Lắng nghe click ngoài board
    useEffect(() => {
        function handleClickOutside(e) {
            if (boardRef.current && !boardRef.current.contains(e.target)) {
                onClose?.();
            }
        }

        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, [onClose]);

    return (
        <div className={styles.overlay}>
            <div ref={boardRef} className={styles.board}>
                <h2 className={styles.title}>Chi tiết sản phẩm</h2>
                <div className={styles.row}><span className={styles.label}>Tên:</span> <span className={styles.value}>{p.productName ?? '-'}</span></div>
                <div className={styles.row}><span className={styles.label}>Giá:</span> <span className={styles.value}>{formatPrice(p.price)}</span></div>
                <div className={styles.row}><span className={styles.label}>Danh mục:</span> <span className={styles.value}>{p.category ?? '-'}</span></div>
                <div className={styles.row}><span className={styles.label}>Số lượng:</span> <span className={styles.value}>{p.quantity ?? '-'}</span></div>
                <div className={`${styles.row} ${styles.descriptionRow}`}>
                    <span className={styles.label}>Mô tả:</span>
                    <span className={styles.value}>{p.description ?? '-'}</span>
                </div>
            </div>
        </div>
    );
}

function formatPrice(price) {
    if (price === null || price === undefined || price === '') return '-';
    if (typeof price === 'number')
        return price.toLocaleString('vi-VN', { style: 'currency', currency: 'VND' });
    return String(price);
}
