import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getProducts, createProduct, updateProduct, deleteProduct } from "../../api/productApi";
import { getCurrentUser, logout } from "../../api/authApi";
import Form from "../../components/Forms/Forms.jsx";
import Header from "../../components/Headers/Header.jsx";
import { addProductModel } from "../../models/addProductFormModel.js";
import { updateProductModel } from "../../models/updateProductFormModel.js";
import ProductList from "./ProductList.jsx";
import Pagination from "../../components/Paginations/Pagination.jsx";
import SortControl from "../../components/Sorts/Sort.jsx";
import styles from "./product.module.css";

export default function ProductManagementPage() {
    const [currentUser, setCurrentUser] = useState(null);
    const [products, setProducts] = useState([]);
    const [editing, setEditing] = useState(null);
    const [showForm, setShowForm] = useState(false);
    const [currProduct,setCurrProduct] = useState(null)
    // 🔹 Thêm state cho phân trang + sort
    const [page, setPage] = useState(0);
    const [limit, setLimit] = useState(5);
    const [totalPages, setTotalPages] = useState(0);
    const [sortBy, setSortBy] = useState("id");
    const [sortDir, setSortDir] = useState("asc");

    const navigate = useNavigate();
    const handleEdit= (p)=>{
        setEditing(true);
        setCurrProduct(p);

    }
    // Lấy danh sách sản phẩm từ API
    const fetchProducts = async () => {
        try {
            const res = await getProducts(page, limit, sortBy, sortDir);
            setProducts(res.data.content || []);
            setTotalPages(res.data.totalPages);
        } catch (err) {
            console.error("Failed to fetch products:", err);
        }
    };

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
    }, [page, limit, sortBy, sortDir]); // 🔹 load lại khi thay đổi

    const handleAdd = async (data) => {
        await createProduct(data);
        setShowForm(false);
        fetchProducts();
    };

    const handleUpdate = async (data) => {
        await updateProduct(currProduct.id, data);
        setEditing(null);
        fetchProducts();
    };

    const handleDelete = async (id) => {
        if (window.confirm("Bạn có chắc muốn xóa sản phẩm này?")) {
            await deleteProduct(id);
            fetchProducts();
        }
    };

    const closeForm = () => {
        setShowForm(false);
        setEditing(null);
    };

    const handleLogout = async () => {
        await logout();
        setCurrentUser(null);
        navigate("/login");
    };

    return (
        <div>
            <Header username={currentUser?.username} onLogout={handleLogout} />

            <div className={styles.container}>
                <h1 className={styles.title}>Quản lý sản phẩm</h1>

                <SortControl sortBy={sortBy} sortDir={sortDir} onChange={(s, d) => { setSortBy(s); setSortDir(d); }} />

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
                                object={editing ? currProduct : null}  // ✅ đúng
                                toCloseForm={closeForm}
                                closeIconDisplay={true}
                                formModel={editing ? updateProductModel : addProductModel}
                                onSubmit={editing ? handleUpdate : handleAdd}
                            />
                        </div>
                    </div>
                )}

                <ProductList
                    products={products}
                    onEdit={handleEdit}
                    onDelete={handleDelete}
                />

                <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
            </div>
        </div>
    );
}

