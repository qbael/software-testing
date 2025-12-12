import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getProducts, createProduct, updateProduct, deleteProduct } from "../../api/productAPI.js";
import { getCurrentUser, logout } from "../../api/authApi";
import Form from "../../components/Forms/Forms.jsx";
import Header from "../../components/Headers/Header.jsx";
import { addProductModel } from "../../models/addProductFormModel.js";
import { updateProductModel } from "../../models/updateProductFormModel.js";
import ProductList from "./productList.jsx";
import Pagination from "../../components/Paginations/Pagination.jsx";
import SortControl from "../../components/Sorts/Sort.jsx";
import styles from "./product.module.css";
import InfoBoard from "../../components/InfoBoard/InfoBoard.jsx";

export default function ProductManagementPage() {
    const [currentUser, setCurrentUser] = useState(null);
    const [products, setProducts] = useState([]);
    const [editing, setEditing] = useState(null);
    const [showForm, setShowForm] = useState(false);
    const [displayBoard,setDisplayBoard] = useState(false)
    const [currProduct,setCurrProduct] = useState(null)
    // 🔹 Thêm state cho phân trang + sort
    const [page, setPage] = useState(0);
    const [limit, setLimit] = useState(5);
    const [totalPages, setTotalPages] = useState(0);
    const [sortBy, setSortBy] = useState("id");
    const [sortDir, setSortDir] = useState("asc");

    const navigate = useNavigate();
    // const isE2E = process.env.REACT_APP_E2E === "true";
    const handleEdit = (p)=>{
        setEditing(true);
        setCurrProduct(p);

    }
    const handleCheck= (p)=>{
        setCurrProduct(p);
        setDisplayBoard(true)
    }
    // Lấy danh sách sản phẩm từ API
    const fetchProducts = async () => {
        try {
            const res = await getProducts(page, limit, sortBy, sortDir);
            setProducts(res.data.content);
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
        alert('Thêm sản phẩm thành công')
        fetchProducts();
    };

    const handleUpdate = async (data) => {
        await updateProduct(currProduct.id, data);
        setEditing(null);
        alert('update sản phẩm thành công')
        fetchProducts();
    };

    const handleDelete = async (id) => {
        if (window.confirm("Bạn có chắc muốn xóa sản phẩm này?")) {
            await deleteProduct(id);
            alert('Xóa sản phẩm thành công')
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

            {displayBoard && (
                <div>
                    {/*{!isE2E && <div className={styles.blurLayer}></div>}*/}
                    <InfoBoard onClose={() => setDisplayBoard(false)} product={currProduct} />
                </div>
            )}

            <div className={styles.container}>
                <h1 className={styles.title} data-testid="page-title">Quản lý sản phẩm</h1>

                {/* Sort Control luôn hiển thị */}
                <select
                    data-testid="sort-by"
                    value={sortBy}
                    onChange={(e) => setSortBy(e.target.value)}
                >
                    <option value="">Chọn</option>
                    <option value="price">Giá</option>
                    <option value="name">Tên</option>
                </select>

                <select
                    data-testid="sort-dir"
                    value={sortDir}
                    onChange={(e) => setSortDir(e.target.value)}
                >
                    <option value="asc">Tăng dần</option>
                    <option value="desc">Giảm dần</option>
                </select>

                {/* Nút Add Product luôn hiển thị nếu không đang show form hoặc edit */}

                    <button
                        className={styles.addButton}
                        onClick={() => setShowForm(true)}
                        data-testid="add-product-btn"
                    >
                        ➕ Thêm sản phẩm
                    </button>


                {/* Form Add / Edit */}
                {(showForm || editing) && (
                    <div>
                        {/*{!isE2E && <div className={styles.blurLayer}></div>}*/}
                        <div className={styles.formWrapper}>
                            <Form
                                object={editing ? currProduct : null}
                                toCloseForm={closeForm}
                                closeIconDisplay={true}
                                formModel={editing ? updateProductModel : addProductModel}
                                onSubmit={editing ? handleUpdate : handleAdd}
                            />
                        </div>
                    </div>
                )}

                {/* Danh sách sản phẩm */}
                <ProductList
                    onDetailCheck={handleCheck}
                    products={products}
                    onEdit={handleEdit}
                    onDelete={handleDelete}
                />

                {/* Phân trang */}
                <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
            </div>
        </div>

    );
}

