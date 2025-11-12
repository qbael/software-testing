import React from "react";
import styles from "./Header.module.css";
import { FaSignOutAlt } from "react-icons/fa"; // icon logout từ react-icons
import avatar from '../../assets/avatar.svg'
const Header = ({ username, onLogout }) => {
    return (
        <header className={styles.header}>
            <div className={styles.userInfo}>
                <img
                    src={avatar} // thay bằng avatar của user nếu có
                    alt="User Avatar"
                    className={styles.avatar}
                />
                <span className={styles.username}>{username}</span>
            </div>
            <button className={styles.logoutBtn} onClick={onLogout}>
                <FaSignOutAlt />
            </button>
        </header>
    );
};

export default Header;
