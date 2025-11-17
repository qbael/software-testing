import { login, getCurrentUser } from '../../api/authApi.js';
import styles from './formPage.module.css'
import {useNavigate} from "react-router-dom";
import backIcon from '../../assets/backIcon.svg'
import Form from "../../components/Forms/Forms.jsx";
import {loginFormModel} from "../../models/loginFormModel.js";

export default function LoginPage() {
    const navigate = useNavigate();

    const onSubmit = async (DataErrorObj) => {

        try {
            // 1️⃣ Gửi request login → backend set cookie JWT
            await login(DataErrorObj.name, DataErrorObj.password);

            // 2️⃣ Gọi API lấy thông tin user từ cookie JWT
            const user = await getCurrentUser();

            alert('Login successfully!');
            navigate('/admin');
        } catch (err) {
            if (err.response?.status === 401) {
                alert('Username or password incorrect');
            } else if (err.response?.status === 400) {
                alert('Invalid input');
            } else {
                alert('Server error, please try again later');
            }
        }
    };

    return (
        <>
            <img
                onClick={() => navigate('/')}
                className={styles.backIcon}
                src={backIcon}
                alt="comeback icon"
            />
            <div className={styles.loginForm}>
                <Form onSubmit={onSubmit} formModel={loginFormModel} />
            </div>
        </>
    );
}
