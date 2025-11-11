import styles from "./Sort.module.css";

export default function SortControl({ sortBy, sortDir, onChange }) {
    return (
        <div className={styles.sortControl}>
            <label>
                Sắp xếp theo:
                <select value={sortBy} onChange={(e) => onChange(e.target.value, sortDir)}>
                    <option value="id">ID</option>
                    <option value="productName">Tên</option>
                    <option value="price">Giá</option>
                    <option value="quantity">Số lượng</option>
                </select>
            </label>
            <label>
                Thứ tự:
                <select value={sortDir} onChange={(e) => onChange(sortBy, e.target.value)}>
                    <option value="asc">Tăng dần</option>
                    <option value="desc">Giảm dần</option>
                </select>
            </label>
        </div>
    );
}
