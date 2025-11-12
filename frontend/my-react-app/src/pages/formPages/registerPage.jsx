import Form from "../../components/Forms/Forms.jsx";
import styles from './formPage.module.css';
import { useNavigate } from 'react-router-dom';
import { registerFormModel } from '../../models/RegisterFormModel';
import backIcon from '../../assets/backIcon.svg';
import { register } from "../../api/authApi.js";

export default function RegisterPage() {
    const navigate = useNavigate();

    const onSubmit = async (obj) => {
        try {
            const response = await register(
                obj.name,
                obj.password,
                obj.confirmPassword
            );
            // Nếu axios, lỗi sẽ ném vào catch, không cần check response.ok
            alert('Đăng ký thành công');
            navigate('/login');
        } catch (error) {
            alert('Đăng ký thất bại, thử lại');
            navigate('/register');
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
            <div className={styles.registerForm}>
                <Form onSubmit={onSubmit} formModel={registerFormModel} />
            </div>
        </>
    );
}

