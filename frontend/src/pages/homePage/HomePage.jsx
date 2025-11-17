import { Link } from 'react-router-dom';
import styles from './HomePage.module.css';

export default function HomePage() {
    return (
        <>
            <div className={styles.homePage}>
                <h1>Welcome To My Ecommerce Website</h1>
                <div>
                    <Link to="/login">
                        <button>Log In</button>
                    </Link>
                    <Link to="/register">
                        <button>Register</button>
                    </Link>
                </div>
            </div>
        </>
    );
}