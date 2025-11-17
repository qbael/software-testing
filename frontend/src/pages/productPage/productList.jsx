import Card from "../../components/Cards/Card.jsx";
import styles from "./product.module.css";

export default function ProductList({ products, onEdit, onDelete }) {
    return (
        <div className={styles.cardGrid}>
            {products.length === 0 ? (
                <p className={styles.empty}>Không có sản phẩm nào.</p>
            ) : (
                products.map((p) => (
                    <Card key={p.id} product={p} onEdit={() => onEdit(p)} onDelete={onDelete} />
                ))
            )}
        </div>
    );
}
