import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getProducts, createProduct, updateProduct, deleteProduct } from "../../api/productApi";
import { getCurrentUser, logout } from "../../api/authApi";
import Form from "../../components/Forms/Forms.jsx";
import Card from "../../components/Cards/Card.jsx";
import Header from "../../components/Headers/Header.jsx";
import { addProductModel } from "../../models/addProductFormModel.js";
import { updateProductModel } from "../../models/updateProductFormModel.js";
import styles from "./product.module.css";

export default function ProductManagementPage() {
    const [currentUser, setCurrentUser] = useState(null);
    const [products, setProducts] = useState([{ id: "1", productName: "Apple iPhone 15", price: 1200, quantity: 10, description: "Newest iPhone with A17 chip", category: "Smartphone", }, { id: "2", productName: "Samsung Galaxy S24", price: 1100, quantity: 15, description: "High-end Android smartphone", category: "Smartphone", }, { id: "3", productName: "MacBook Pro 16", price: 2500, quantity: 5, description: "Powerful laptop for professionals", category: "Laptop", }]);
    const [editing, setEditing] = useState(null);
    const [showForm, setShowForm] = useState(false);
    const navigate = useNavigate();

    // Lấy danh sách sản phẩm từ API
    const fetchProducts = async () => {
        try {
            const res = await getProducts();
            setProducts(res.data.content || []);
        } catch (err) {
            console.error("Failed to fetch products:", err);
        }
    };

    // Lấy user hiện tại từ JWT
    const fetchUser = async () => {
        try {
            const user = await getCurrentUser();
            setCurrentUser(user);
        } catch (err) {
            console.error("User not authenticated:", err);
            // navigate("/login");
        }
    };

    useEffect(() => {
        fetchProducts();
        fetchUser();
    }, []);

    // Thêm sản phẩm
    const handleAdd = async (data) => {
        try {
            await createProduct(data);
            setShowForm(false);
            fetchProducts();
        } catch (err) {
            console.error("Failed to add product:", err);
        }
    };

    // Cập nhật sản phẩm
    const handleUpdate = async (data) => {
        try {
            await updateProduct(editing.id, data);
            setEditing(null);
            fetchProducts();
        } catch (err) {
            console.error("Failed to update product:", err);
        }
    };

    // Xóa sản phẩm
    const handleDelete = async (id) => {
        if (window.confirm("Bạn có chắc muốn xóa sản phẩm này?")) {
            try {
                await deleteProduct(id);
                fetchProducts();
            } catch (err) {
                console.error("Failed to delete product:", err);
            }
        }
    };

    // Đóng form
    const closeForm = () => {
        setShowForm(false);
        setEditing(null);
    };

    // Logout
    const handleLogout = async () => {
        try {
            await logout();
            setCurrentUser(null);
            navigate("/login");
        } catch (err) {
            console.error("Logout failed:", err);
        }
    };

    return (
        <div>
            <Header username={currentUser?.username} onLogout={handleLogout} />

            <div className={styles.container}>
                <h1 className={styles.title}>Quản lý sản phẩm</h1>

                {!showForm && !editing && (
                    <button className={styles.addButton} onClick={() => setShowForm(true)}>
                        ➕ Thêm sản phẩm
                    </button>
                )}

                {(showForm || editing) && (
                    <div>
                        <div className={styles.blurLayer}></div>
                        <div className={styles.formWrapper}>
                            <Form
                                toCloseForm={closeForm}
                                closeIconDisplay={true}
                                formModel={editing ? updateProductModel : addProductModel}
                                onSubmit={editing ? handleUpdate : handleAdd}
                            />
                        </div>
                    </div>
                )}

                <div className={styles.cardGrid}>
                    {products.length === 0 ? (
                        <p className={styles.empty}>Không có sản phẩm nào.</p>
                    ) : (
                        products.map((p) => (
                            <Card
                                key={p.id}
                                product={p}
                                onEdit={() => setEditing(p)}
                                onDelete={handleDelete}
                            />
                        ))
                    )}
                </div>
            </div>
        </div>
    );
}
