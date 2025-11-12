import styles from './Card.module.css';
import infoIcon from '../../../public/infoIcon.svg'
export default function Card({ product, onEdit, onDelete,onDetailCheck }) {
    return (
        <div className={styles.card}>
            <div>
                <div className={styles.flexContainer}>
                    <h2 className={styles.name}>{product.productName}</h2>
                    <img onClick={()=>onDetailCheck(product)} className={styles.icon} src={infoIcon} alt={"icon"}/>
                </div>

                <p className={styles.price}>
                    💰 <span>{product.price.toLocaleString()}₫</span>
                </p>
                <p className={styles.info}>📦 Số lượng: {product.quantity}</p>
                <p className={styles.info}>🏷 Danh mục: {product.category}</p>
                {product.description && (
                    <p className={styles.description}>“{product.description}”</p>
                )}
            </div>

            <div className={styles.actions}>
                <button className={styles.editBtn} onClick={() => onEdit(product)}>
                    ✏️ Sửa
                </button>
                <button className={styles.deleteBtn} onClick={() => onDelete(product.id)}>
                    🗑 Xóa
                </button>
            </div>
        </div>
    );
}
