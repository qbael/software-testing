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
            await login(DataErrorObj.name, DataErrorObj.password);

            const user = await getCurrentUser();

            alert('Đăng nhập thành công!');
            navigate('/admin');
        } catch (err) {
            const msg = err.response?.data || 'Đăng ký thất bại. Vui lòng thử lại.'
            alert(msg)
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
